package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.EstadoCancha;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.MotivoCancelacion;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ReservaUseCase {

    private static final Logger LOG = Logger.getLogger(ReservaUseCase.class.getName());

    static final int MAX_HORAS = 3;
    static final int MAX_DIAS_ANTICIPACION = 30;
    static final int MAX_RESERVAS_FUTURAS = 3;
    static final long HORAS_MINIMAS_PARA_CANCELAR = 2;
    private static final int NUM_CANDADOS = 64;

    private final ReservaGateway reservas;
    private final UsuarioServiceGateway usuarios;
    private final CanchaServiceGateway canchas;
    private final NotificadorReserva notificador;
    private final ValidadorAdmin admin;
    private final Clock clock;
    /** Evita doble reserva de la misma franja con peticiones simultáneas (válido para una instancia). */
    private final Object[] candados = new Object[NUM_CANDADOS];

    public ReservaUseCase(ReservaGateway reservas, UsuarioServiceGateway usuarios, CanchaServiceGateway canchas,
                          NotificadorReserva notificador, ValidadorAdmin admin, Clock clock) {
        this.reservas = reservas;
        this.usuarios = usuarios;
        this.canchas = canchas;
        this.notificador = notificador;
        this.admin = admin;
        this.clock = clock;
        for (int i = 0; i < NUM_CANDADOS; i++) {
            candados[i] = new Object();
        }
    }

    // ------------------------------------------------------------------ crear

    public Reserva crear(Long canchaId, Long usuarioId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        requerir(canchaId, usuarioId, fecha, horaInicio, horaFin);
        // 1 y 2: formato y fecha
        validarFormato(horaInicio, horaFin);
        validarFecha(fecha, horaInicio);
        // 3, 4 y 5: auth, canchas (estado) y canchas (/horario)
        Contexto contexto = validarUsuarioYCancha(usuarioId, canchaId, fecha, horaInicio, horaFin);

        Reserva guardada;
        synchronized (candado(canchaId, fecha)) {
            // 6: base de datos
            validarSolapamiento(canchaId, fecha, horaInicio, horaFin, null);
            if (reservas.contarConfirmadasFuturas(usuarioId, ahora()) >= MAX_RESERVAS_FUTURAS) {
                throw new ReservasException(CodigoError.LIMITE_RESERVAS,
                        "El usuario ya tiene " + MAX_RESERVAS_FUTURAS + " reservas confirmadas a futuro");
            }
            // 7: tarifa
            long horas = Duration.between(horaInicio, horaFin).toHours();
            long precioTotal = canchas.obtenerPrecioPorHora(canchaId, fecha) * horas;
            // 8: guardar
            guardada = reservas.guardar(new Reserva(null, canchaId, usuarioId, fecha, horaInicio, horaFin,
                    precioTotal, EstadoReserva.CONFIRMADA, null, ahora().truncatedTo(ChronoUnit.SECONDS)));
        }
        // 9: notificar en segundo plano (best effort)
        notificador.confirmacion(guardada, contexto.usuario(), contexto.cancha());
        return guardada;
    }

    // -------------------------------------------------------------- consultas

    public List<Reserva> listarTodas() {
        return reservas.listarTodas();
    }

    public Reserva obtener(Long id) {
        return reservas.buscarPorId(id).orElseThrow(() ->
                new ReservasException(CodigoError.RESERVA_NO_ENCONTRADA, "La reserva " + id + " no existe"));
    }

    public void eliminar(Long id) {
        obtener(id);
        reservas.eliminar(id);
    }

    /** Más recientes primero. */
    public List<Reserva> historialDeUsuario(Long usuarioId) {
        return reservas.buscarPorUsuario(usuarioId);
    }

    /** Reservas CONFIRMADAS del día ordenadas por horaInicio. */
    public List<Reserva> agendaDeCancha(Long canchaId, LocalDate fecha) {
        requerir(canchaId, fecha);
        return reservas.buscarConfirmadasPorCanchaYFecha(canchaId, fecha);
    }

    // --------------------------------------------------------------- cancelar

    public Reserva cancelar(Long id, MotivoCancelacion motivo) {
        MotivoCancelacion motivoFinal = motivo == null ? MotivoCancelacion.USUARIO : motivo;
        Reserva reserva = obtener(id);
        if (reserva.estado() != EstadoReserva.CONFIRMADA) {
            throw new ReservasException(CodigoError.RESERVA_NO_CANCELABLE,
                    "Solo se pueden cancelar reservas CONFIRMADAS (estado actual: " + reserva.estado() + ")");
        }
        if (motivoFinal != MotivoCancelacion.LLUVIA) {
            Duration faltante = Duration.between(ahora(), reserva.inicio());
            if (faltante.compareTo(Duration.ofHours(HORAS_MINIMAS_PARA_CANCELAR)) < 0) {
                throw new ReservasException(CodigoError.CANCELACION_TARDIA,
                        "Solo se puede cancelar hasta " + HORAS_MINIMAS_PARA_CANCELAR + " horas antes del inicio");
            }
        }
        Reserva cancelada = reservas.guardar(reserva.cancelada(motivoFinal));
        notificarCancelacion(cancelada, motivoFinal);
        return cancelada;
    }

    // ------------------------------------------------------------ reprogramar

    public Reserva reprogramar(Long id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        requerir(id, fecha, horaInicio, horaFin);
        Reserva original = obtener(id);
        validarFormato(horaInicio, horaFin);
        validarFecha(fecha, horaInicio);
        if (original.estado() != EstadoReserva.CONFIRMADA) {
            throw new ReservasException(CodigoError.ESTADO_RESERVA_INVALIDO,
                    "Solo se pueden reprogramar reservas CONFIRMADAS (estado actual: " + original.estado() + ")");
        }
        Contexto contexto = validarUsuarioYCancha(original.usuarioId(), original.canchaId(), fecha, horaInicio, horaFin);

        Reserva actualizada;
        synchronized (candado(original.canchaId(), fecha)) {
            // El límite de 3 reservas no se vuelve a validar: reprogramar no cambia la cantidad.
            validarSolapamiento(original.canchaId(), fecha, horaInicio, horaFin, original.id());
            long horas = Duration.between(horaInicio, horaFin).toHours();
            long precioTotal = canchas.obtenerPrecioPorHora(original.canchaId(), fecha) * horas;
            actualizada = reservas.guardar(original.reprogramada(fecha, horaInicio, horaFin, precioTotal));
        }
        notificador.reprogramacion(actualizada, contexto.usuario(), contexto.cancha(),
                original.fecha(), original.horaInicio());
        return actualizada;
    }

    // ------------------------------------------------------------- asistencia

    public Reserva registrarAsistencia(Long adminId, Long id, EstadoReserva nuevoEstado) {
        admin.exigirAdmin(adminId);
        if (nuevoEstado != EstadoReserva.COMPLETADA && nuevoEstado != EstadoReserva.NO_ASISTIO) {
            throw new ReservasException(CodigoError.VALIDACION, "El estado debe ser COMPLETADA o NO_ASISTIO");
        }
        Reserva reserva = obtener(id);
        if (reserva.estado() != EstadoReserva.CONFIRMADA) {
            throw new ReservasException(CodigoError.ESTADO_RESERVA_INVALIDO,
                    "Solo se registra asistencia de reservas CONFIRMADAS (estado actual: " + reserva.estado() + ")");
        }
        if (reserva.inicio().isAfter(ahora())) {
            throw new ReservasException(CodigoError.ESTADO_RESERVA_INVALIDO,
                    "La reserva aún no ha empezado");
        }
        return reservas.guardar(reserva.conEstado(nuevoEstado));
    }

    // ----------------------------------------------------------------- reglas

    private void validarFormato(LocalTime inicio, LocalTime fin) {
        if (!inicio.equals(inicio.truncatedTo(ChronoUnit.HOURS)) || !fin.equals(fin.truncatedTo(ChronoUnit.HOURS))) {
            throw new ReservasException(CodigoError.DURACION_INVALIDA, "Las horas deben ser en punto (minutos 00)");
        }
        if (!fin.isAfter(inicio)) {
            throw new ReservasException(CodigoError.DURACION_INVALIDA, "horaFin debe ser posterior a horaInicio");
        }
        long horas = Duration.between(inicio, fin).toHours();
        if (horas > MAX_HORAS) {
            throw new ReservasException(CodigoError.DURACION_INVALIDA,
                    "La reserva debe durar entre 1 y " + MAX_HORAS + " horas");
        }
    }

    private void validarFecha(LocalDate fecha, LocalTime horaInicio) {
        LocalDateTime ahora = ahora();
        LocalDate hoy = ahora.toLocalDate();
        if (fecha.isBefore(hoy) || LocalDateTime.of(fecha, horaInicio).isBefore(ahora)) {
            throw new ReservasException(CodigoError.FECHA_INVALIDA, "No se puede reservar en el pasado");
        }
        if (fecha.isAfter(hoy.plusDays(MAX_DIAS_ANTICIPACION))) {
            throw new ReservasException(CodigoError.FECHA_INVALIDA,
                    "Solo se puede reservar con máximo " + MAX_DIAS_ANTICIPACION + " días de anticipación");
        }
    }

    /** Pasos 3, 4 y 5 del orden de validaciones; comunes a crear y reprogramar. */
    private Contexto validarUsuarioYCancha(Long usuarioId, Long canchaId, LocalDate fecha, LocalTime inicio,
                                           LocalTime fin) {
        UsuarioInfo usuario = usuarios.validar(usuarioId);
        if (!usuario.activo()) {
            throw new ReservasException(CodigoError.USUARIO_INACTIVO, "El usuario está inactivo");
        }
        CanchaInfo cancha = canchas.obtenerCancha(canchaId);
        if (cancha.estado() != EstadoCancha.ACTIVA) {
            throw new ReservasException(CodigoError.CANCHA_NO_DISPONIBLE,
                    "La cancha no admite reservas (estado " + cancha.estado() + ")");
        }
        HorarioCancha horario = canchas.obtenerHorario(canchaId, fecha);
        if (!horario.contiene(inicio, fin)) {
            throw new ReservasException(CodigoError.FUERA_DE_HORARIO,
                    "La franja está fuera del horario de la cancha (" + horario.horaApertura() + " a "
                            + horario.horaCierre() + ")");
        }
        if (horario.chocaConBloqueo(inicio, fin)) {
            throw new ReservasException(CodigoError.CANCHA_NO_DISPONIBLE, "La franja choca con un bloqueo de la cancha");
        }
        return new Contexto(usuario, cancha);
    }

    private void validarSolapamiento(Long canchaId, LocalDate fecha, LocalTime inicio, LocalTime fin, Long excluirId) {
        if (reservas.existeSolapamiento(canchaId, fecha, inicio, fin, excluirId)) {
            throw new ReservasException(CodigoError.FRANJA_OCUPADA, "La franja ya está reservada");
        }
    }

    /** Si no se puede armar la notificación (auth o canchas caídos), se registra y se sigue. */
    private void notificarCancelacion(Reserva cancelada, MotivoCancelacion motivo) {
        try {
            UsuarioInfo usuario = usuarios.validar(cancelada.usuarioId());
            CanchaInfo cancha = canchas.obtenerCancha(cancelada.canchaId());
            notificador.cancelacion(cancelada, usuario, cancha, motivo);
        } catch (ReservasException e) {
            LOG.warning("Reserva " + cancelada.id() + " cancelada sin notificar: " + e.getMessage());
        }
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    private Object candado(Long canchaId, LocalDate fecha) {
        return candados[Math.floorMod(Objects.hash(canchaId, fecha), NUM_CANDADOS)];
    }

    private static void requerir(Object... valores) {
        for (Object valor : valores) {
            if (valor == null) {
                throw new ReservasException(CodigoError.VALIDACION, "Faltan datos obligatorios");
            }
        }
    }

    private record Contexto(UsuarioInfo usuario, CanchaInfo cancha) {
    }
}

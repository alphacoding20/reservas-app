package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.Pronostico;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.ResumenAlertaLluvia;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ClimaGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;

import java.time.LocalDate;
import java.util.Optional;

public class PronosticoUseCase {

    private final ReservaGateway reservas;
    private final UsuarioServiceGateway usuarios;
    private final CanchaServiceGateway canchas;
    private final ClimaGateway clima;
    private final NotificadorReserva notificador;
    private final ValidadorAdmin admin;
    private final int umbral;

    public PronosticoUseCase(ReservaGateway reservas, UsuarioServiceGateway usuarios, CanchaServiceGateway canchas,
                             ClimaGateway clima, NotificadorReserva notificador, ValidadorAdmin admin, int umbral) {
        this.reservas = reservas;
        this.usuarios = usuarios;
        this.canchas = canchas;
        this.clima = clima;
        this.notificador = notificador;
        this.admin = admin;
        this.umbral = umbral;
    }

    public Pronostico pronosticoDeReserva(Long reservaId) {
        Reserva reserva = reservas.buscarPorId(reservaId).orElseThrow(() ->
                new ReservasException(CodigoError.RESERVA_NO_ENCONTRADA, "La reserva " + reservaId + " no existe"));
        CanchaInfo cancha = canchas.obtenerCancha(reserva.canchaId());
        return evaluar(reserva, cancha);
    }

    public ResumenAlertaLluvia alertarLluvia(Long adminId, LocalDate fecha) {
        admin.exigirAdmin(adminId);
        if (fecha == null) {
            throw new ReservasException(CodigoError.VALIDACION, "La fecha es obligatoria");
        }
        DatosExternos externos = new DatosExternos(usuarios, canchas);
        int revisadas = 0;
        int enviadas = 0;
        int sinPronostico = 0;

        for (Reserva reserva : reservas.buscarConfirmadasPorFecha(fecha)) {
            Optional<CanchaInfo> cancha = externos.cancha(reserva.canchaId());
            if (cancha.isPresent() && cancha.get().techada()) {
                continue; // las canchas techadas no se revisan
            }
            revisadas++;
            if (cancha.isEmpty()) {
                sinPronostico++;
                continue;
            }
            Pronostico pronostico = evaluar(reserva, cancha.get());
            if (!pronostico.pronosticoDisponible()) {
                sinPronostico++;
            } else if (pronostico.riesgoLluvia()) {
                Optional<UsuarioInfo> usuario = externos.usuario(reserva.usuarioId());
                if (usuario.isPresent()) {
                    notificador.alertaLluvia(reserva, usuario.get(), cancha.get(), pronostico.probabilidadLluvia());
                    enviadas++;
                }
            }
        }
        return new ResumenAlertaLluvia(fecha, revisadas, enviadas, sinPronostico);
    }

    private Pronostico evaluar(Reserva reserva, CanchaInfo cancha) {
        if (cancha.techada()) {
            return new Pronostico(reserva.id(), false, true, null, false, umbral);
        }
        Optional<Integer> probabilidad = cancha.tieneCoordenadas()
                ? clima.consultarProbabilidadLluvia(cancha.latitud(), cancha.longitud(), reserva.fecha(),
                reserva.horaInicio(), reserva.horaFin())
                : Optional.empty();
        if (probabilidad.isEmpty()) {
            return new Pronostico(reserva.id(), true, false, null, false, umbral);
        }
        int valor = probabilidad.get();
        return new Pronostico(reserva.id(), true, true, valor, valor >= umbral, umbral);
    }
}

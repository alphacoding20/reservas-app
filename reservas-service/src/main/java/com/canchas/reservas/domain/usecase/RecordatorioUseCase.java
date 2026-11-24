package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.domain.model.UsuarioInfo;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;

import java.time.LocalDate;
import java.util.Optional;

public class RecordatorioUseCase {

    private final ReservaGateway reservas;
    private final UsuarioServiceGateway usuarios;
    private final CanchaServiceGateway canchas;
    private final NotificadorReserva notificador;
    private final ValidadorAdmin admin;

    public RecordatorioUseCase(ReservaGateway reservas, UsuarioServiceGateway usuarios, CanchaServiceGateway canchas,
                               NotificadorReserva notificador, ValidadorAdmin admin) {
        this.reservas = reservas;
        this.usuarios = usuarios;
        this.canchas = canchas;
        this.notificador = notificador;
        this.admin = admin;
    }

    public ResumenRecordatorios enviarManual(Long adminId, LocalDate fecha) {
        admin.exigirAdmin(adminId);
        return enviarParaFecha(fecha);
    }

    public ResumenRecordatorios enviarParaFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ReservasException(CodigoError.VALIDACION, "La fecha es obligatoria");
        }
        DatosExternos externos = new DatosExternos(usuarios, canchas);
        int enviados = 0;
        for (Reserva reserva : reservas.buscarConfirmadasPorFecha(fecha)) {
            Optional<UsuarioInfo> usuario = externos.usuario(reserva.usuarioId());
            Optional<CanchaInfo> cancha = externos.cancha(reserva.canchaId());
            if (usuario.isPresent() && cancha.isPresent()) {
                notificador.recordatorio(reserva, usuario.get(), cancha.get());
                enviados++;
            }
        }
        return new ResumenRecordatorios(fecha, enviados);
    }
}

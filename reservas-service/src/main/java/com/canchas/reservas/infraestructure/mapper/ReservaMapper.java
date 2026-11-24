package com.canchas.reservas.infraestructure.mapper;

import com.canchas.reservas.domain.model.Reserva;
import com.canchas.reservas.infraestructure.driver_adapters.jpa_repository.ReservaData;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toReserva(ReservaData data) {
        return new Reserva(data.getId(), data.getCanchaId(), data.getUsuarioId(), data.getFecha(),
                data.getHoraInicio(), data.getHoraFin(), data.getPrecioTotal(), data.getEstado(),
                data.getMotivoCancelacion(), data.getFechaCreacion());
    }

    public ReservaData toData(Reserva reserva) {
        ReservaData data = new ReservaData();
        data.setId(reserva.id());
        data.setCanchaId(reserva.canchaId());
        data.setUsuarioId(reserva.usuarioId());
        data.setFecha(reserva.fecha());
        data.setHoraInicio(reserva.horaInicio());
        data.setHoraFin(reserva.horaFin());
        data.setPrecioTotal(reserva.precioTotal());
        data.setEstado(reserva.estado());
        data.setMotivoCancelacion(reserva.motivoCancelacion());
        data.setFechaCreacion(reserva.fechaCreacion());
        return data;
    }
}

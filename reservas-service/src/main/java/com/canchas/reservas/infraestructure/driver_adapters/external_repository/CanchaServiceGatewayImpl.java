package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.Bloqueo;
import com.canchas.reservas.domain.model.CanchaInfo;
import com.canchas.reservas.domain.model.HorarioCancha;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.CanchaResponse;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.HorarioResponse;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.TarifaResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Component
public class CanchaServiceGatewayImpl implements CanchaServiceGateway {

    private final RestClient client;

    public CanchaServiceGatewayImpl(@Qualifier("canchasRestClient") RestClient client) {
        this.client = client;
    }

    @Override
    public CanchaInfo obtenerCancha(Long canchaId) {
        CanchaResponse r = llamar(canchaId, () -> client.get()
                .uri("/api/canchas/{id}", canchaId)
                .retrieve()
                .body(CanchaResponse.class));
        return new CanchaInfo(r.id(), r.nombre(), r.latitud(), r.longitud(), Boolean.TRUE.equals(r.techada()),
                r.estado());
    }

    @Override
    public HorarioCancha obtenerHorario(Long canchaId, LocalDate fecha) {
        HorarioResponse r = llamar(canchaId, () -> client.get()
                .uri("/api/canchas/{id}/horario?fecha={fecha}", canchaId, fecha.toString())
                .retrieve()
                .body(HorarioResponse.class));
        List<HorarioResponse.BloqueoResponse> bloqueos = r.bloqueos() == null ? List.of() : r.bloqueos();
        return new HorarioCancha(canchaId, fecha, r.estado(), r.horaApertura(), r.horaCierre(),
                bloqueos.stream().map(b -> new Bloqueo(b.horaInicio(), b.horaFin(), b.motivo())).toList());
    }

    @Override
    public long obtenerPrecioPorHora(Long canchaId, LocalDate fecha) {
        TarifaResponse r = llamar(canchaId, () -> client.get()
                .uri("/api/canchas/{id}/tarifa?fecha={fecha}", canchaId, fecha.toString())
                .retrieve()
                .body(TarifaResponse.class));
        if (r.precioPorHora() == null) {
            throw new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "canchas-service no devolvió precioPorHora");
        }
        return r.precioPorHora();
    }

    /** 404 -> CANCHA_NO_ENCONTRADA; cualquier otro fallo o cuerpo vacío -> SERVICIO_NO_DISPONIBLE (502). */
    private <T> T llamar(Long canchaId, Supplier<T> llamada) {
        T respuesta;
        try {
            respuesta = llamada.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ReservasException(CodigoError.CANCHA_NO_ENCONTRADA, "La cancha " + canchaId + " no existe");
        } catch (RestClientException e) {
            throw new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "canchas-service no disponible");
        }
        if (respuesta == null) {
            throw new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE,
                    "canchas-service devolvió una respuesta vacía");
        }
        return respuesta;
    }
}

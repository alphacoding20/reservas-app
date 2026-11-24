package com.reservasapp.canchas.infraestructure.driver_adapters.external;

import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.gateway.FestivoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NagerFestivoGateway implements FestivoGateway {

    private final RestClient restClient;
    private final Map<Integer, List<Festivo>> cache = new ConcurrentHashMap<>();

    public NagerFestivoGateway(@Qualifier("nagerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Festivo> festivosDelAnio(int anio) {
        List<Festivo> enCache = cache.get(anio);
        if (enCache != null) {
            return enCache;
        }
        try {
            NagerHoliday[] respuesta = restClient.get()
                    .uri("/PublicHolidays/{anio}/CO", anio)
                    .retrieve()
                    .body(NagerHoliday[].class);
            List<Festivo> festivos = respuesta == null ? List.of() : Arrays.stream(respuesta)
                    .filter(h -> h.date() != null)
                    .map(h -> new Festivo(LocalDate.parse(h.date()), Objects.toString(h.localName(), "")))
                    .toList();
            cache.put(anio, festivos);
            return festivos;
        } catch (RestClientException e) {
            log.warn("Nager.Date no respondió para el año {}: {}", anio, e.getMessage());
            return List.of();
        }
    }
}

package com.canchas.reservas.infraestructure.driver_adapters.external_repository;

import com.canchas.reservas.domain.model.gateway.ClimaGateway;
import com.canchas.reservas.infraestructure.driver_adapters.external_repository.dto.OpenMeteoResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ClimaGatewayImpl implements ClimaGateway {

    private static final Logger LOG = Logger.getLogger(ClimaGatewayImpl.class.getName());

    private final RestClient client;
    private final Clock clock;
    private final int maxDiasPronostico;
    /** Clave: "lat|lon|fecha". Valor: hora del día (0-23) -> probabilidad de precipitación. */
    private final Cache<String, Map<Integer, Integer>> cache;

    public ClimaGatewayImpl(@Qualifier("climaRestClient") RestClient client, Clock clock,
                            @Value("${reservas.clima.cache-minutos}") long cacheMinutos,
                            @Value("${reservas.clima.max-dias-pronostico}") int maxDiasPronostico) {
        this.client = client;
        this.clock = clock;
        this.maxDiasPronostico = maxDiasPronostico;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(cacheMinutos))
                .maximumSize(1_000)
                .build();
    }

    @Override
    public Optional<Integer> consultarProbabilidadLluvia(Double latitud, Double longitud, LocalDate fecha,
                                                         LocalTime horaInicio, LocalTime horaFin) {
        if (latitud == null || longitud == null) {
            return Optional.empty();
        }
        if (fecha.isAfter(LocalDate.now(clock).plusDays(maxDiasPronostico))) {
            return Optional.empty(); // fuera del rango que cubre Open-Meteo: ni siquiera se consulta
        }
        String clave = String.format(Locale.ROOT, "%.2f|%.2f|%s", latitud, longitud, fecha);
        Map<Integer, Integer> porHora = cache.getIfPresent(clave);
        if (porHora == null) {
            porHora = descargar(latitud, longitud, fecha);
            if (porHora.isEmpty()) {
                return Optional.empty();
            }
            cache.put(clave, porHora);
        }
        return porHora.entrySet().stream()
                .filter(e -> e.getKey() >= horaInicio.getHour() && e.getKey() < horaFin.getHour())
                .map(Map.Entry::getValue)
                .max(Integer::compare);
    }

    /** Devuelve un mapa vacío si no hay datos utilizables. */
    private Map<Integer, Integer> descargar(Double latitud, Double longitud, LocalDate fecha) {
        try {
            OpenMeteoResponse respuesta = client.get()
                    .uri("/forecast?latitude={lat}&longitude={lon}&hourly=precipitation_probability"
                                    + "&timezone=America/Bogota&start_date={inicio}&end_date={fin}",
                            String.valueOf(latitud), String.valueOf(longitud), fecha.toString(), fecha.toString())
                    .retrieve()
                    .body(OpenMeteoResponse.class);
            return aMapaPorHora(respuesta);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Open-Meteo no disponible, se responde sin pronóstico: " + e.getMessage());
            return Map.of();
        }
    }

    private Map<Integer, Integer> aMapaPorHora(OpenMeteoResponse respuesta) {
        Map<Integer, Integer> porHora = new HashMap<>();
        if (respuesta == null || respuesta.hourly() == null || respuesta.hourly().time() == null
                || respuesta.hourly().precipitationProbability() == null) {
            return porHora;
        }
        var horas = respuesta.hourly().time();
        var probabilidades = respuesta.hourly().precipitationProbability();
        for (int i = 0; i < Math.min(horas.size(), probabilidades.size()); i++) {
            Integer probabilidad = probabilidades.get(i);
            if (probabilidad != null) {
                porHora.put(LocalDateTime.parse(horas.get(i)).getHour(), probabilidad);
            }
        }
        return porHora;
    }
}

package com.canchas.reservas.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Bean
    public Clock clock() {
        return Clock.system(ZONA);
    }

    @Bean(name = "notificacionExecutor")
    public Executor notificacionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }

    @Bean
    @Qualifier("authRestClient")
    public RestClient authRestClient(@Value("${reservas.auth.url}") String url,
                                     @Value("${reservas.timeout-ms}") long timeoutMs) {
        return crearCliente(url, timeoutMs);
    }

    @Bean
    @Qualifier("canchasRestClient")
    public RestClient canchasRestClient(@Value("${reservas.canchas.url}") String url,
                                        @Value("${reservas.timeout-ms}") long timeoutMs) {
        return crearCliente(url, timeoutMs);
    }

    @Bean
    @Qualifier("notificacionesRestClient")
    public RestClient notificacionesRestClient(@Value("${reservas.notificaciones.url}") String url,
                                               @Value("${reservas.timeout-ms}") long timeoutMs) {
        return crearCliente(url, timeoutMs);
    }

    @Bean
    @Qualifier("climaRestClient")
    public RestClient climaRestClient(@Value("${reservas.openmeteo.url}") String url,
                                      @Value("${reservas.timeout-ms}") long timeoutMs) {
        return crearCliente(url, timeoutMs);
    }

    private RestClient crearCliente(String url, long timeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder()
                .baseUrl(url.strip().replaceAll("/+$", ""))
                .requestFactory(factory)
                .build();
    }
}

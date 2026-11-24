package com.reservasapp.notificaciones.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }


    @Bean
    public RestClient telegramRestClient(@Value("${telegram.api-url}") String apiUrl,
                                         @Value("${telegram.timeout-ms:2000}") long timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build());
        fabrica.setReadTimeout(timeout);
        return RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(fabrica)
                .build();
    }
}

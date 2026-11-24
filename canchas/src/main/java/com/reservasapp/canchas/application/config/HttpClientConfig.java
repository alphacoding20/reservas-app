package com.reservasapp.canchas.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean("authRestClient")
    public RestClient authRestClient(@Value("${auth.url}") String baseUrl,
                                     @Value("${http.timeout-ms}") int timeoutMs) {
        return crear(baseUrl, timeoutMs);
    }

    @Bean("nagerRestClient")
    public RestClient nagerRestClient(@Value("${nager.url}") String baseUrl,
                                      @Value("${http.timeout-ms}") int timeoutMs) {
        return crear(baseUrl, timeoutMs);
    }

    private RestClient crear(String baseUrl, int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}

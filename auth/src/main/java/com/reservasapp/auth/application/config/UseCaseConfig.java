package com.reservasapp.auth.application.config;

import com.reservasapp.auth.domain.model.gateway.ClaveGateway;
import com.reservasapp.auth.domain.model.gateway.UsuarioGateway;
import com.reservasapp.auth.domain.usecase.UsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateway usuarioGateway, ClaveGateway claveGateway) {
        return new UsuarioUseCase(usuarioGateway, claveGateway);
    }
}

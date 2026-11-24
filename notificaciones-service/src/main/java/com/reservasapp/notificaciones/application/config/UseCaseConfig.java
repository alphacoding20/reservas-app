package com.reservasapp.notificaciones.application.config;

import com.reservasapp.notificaciones.domain.model.gateway.EnvioGateway;
import com.reservasapp.notificaciones.domain.model.gateway.NotificacionGateway;
import com.reservasapp.notificaciones.domain.usecase.ConsultarNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.DespachadorEnvio;
import com.reservasapp.notificaciones.domain.usecase.EnviarNotificacionUseCase;
import com.reservasapp.notificaciones.domain.usecase.EstadisticasNotificacionesUseCase;
import com.reservasapp.notificaciones.domain.usecase.ReenviarNotificacionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class UseCaseConfig {

    @Bean
    public DespachadorEnvio despachadorEnvio(List<EnvioGateway> envioGateways) {
        return new DespachadorEnvio(envioGateways);
    }

    @Bean
    public EnviarNotificacionUseCase enviarNotificacionUseCase(NotificacionGateway gateway,
                                                               DespachadorEnvio despachador, Clock clock) {
        return new EnviarNotificacionUseCase(gateway, despachador, clock);
    }

    @Bean
    public ReenviarNotificacionUseCase reenviarNotificacionUseCase(NotificacionGateway gateway,
                                                                   DespachadorEnvio despachador) {
        return new ReenviarNotificacionUseCase(gateway, despachador);
    }

    @Bean
    public ConsultarNotificacionesUseCase consultarNotificacionesUseCase(NotificacionGateway gateway) {
        return new ConsultarNotificacionesUseCase(gateway);
    }

    @Bean
    public EstadisticasNotificacionesUseCase estadisticasNotificacionesUseCase(NotificacionGateway gateway) {
        return new EstadisticasNotificacionesUseCase(gateway);
    }
}

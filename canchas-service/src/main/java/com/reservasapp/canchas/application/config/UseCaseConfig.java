package com.reservasapp.canchas.application.config;

import com.reservasapp.canchas.domain.model.gateway.AuthGateway;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import com.reservasapp.canchas.domain.model.gateway.FestivoGateway;
import com.reservasapp.canchas.domain.usecase.AutorizacionUseCase;
import com.reservasapp.canchas.domain.usecase.BloqueoUseCase;
import com.reservasapp.canchas.domain.usecase.CanchaUseCase;
import com.reservasapp.canchas.domain.usecase.HorarioUseCase;
import com.reservasapp.canchas.domain.usecase.TarifaUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class UseCaseConfig {

    @Bean
    public AutorizacionUseCase autorizacionUseCase(AuthGateway authGateway) {
        return new AutorizacionUseCase(authGateway);
    }

    @Bean
    public CanchaUseCase canchaUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        return new CanchaUseCase(canchaGateway, bloqueoGateway);
    }

    @Bean
    public BloqueoUseCase bloqueoUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        return new BloqueoUseCase(canchaGateway, bloqueoGateway);
    }

    @Bean
    public HorarioUseCase horarioUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        return new HorarioUseCase(canchaGateway, bloqueoGateway);
    }

    @Bean
    public TarifaUseCase tarifaUseCase(CanchaGateway canchaGateway,
                                       FestivoGateway festivoGateway,
                                       @Value("${tarifa.factor-recargo}") BigDecimal factorRecargo) {
        return new TarifaUseCase(canchaGateway, festivoGateway, factorRecargo);
    }
}

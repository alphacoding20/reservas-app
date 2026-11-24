package com.canchas.reservas.application.config;

import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.ClimaGateway;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import com.canchas.reservas.domain.usecase.DisponibilidadUseCase;
import com.canchas.reservas.domain.usecase.EstadisticasUseCase;
import com.canchas.reservas.domain.usecase.NotificadorReserva;
import com.canchas.reservas.domain.usecase.PronosticoUseCase;
import com.canchas.reservas.domain.usecase.RecordatorioUseCase;
import com.canchas.reservas.domain.usecase.ReservaUseCase;
import com.canchas.reservas.domain.usecase.ValidadorAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfig {

    @Bean
    public ValidadorAdmin validadorAdmin(UsuarioServiceGateway usuarioServiceGateway) {
        return new ValidadorAdmin(usuarioServiceGateway);
    }

    @Bean
    public NotificadorReserva notificadorReserva(NotificacionServiceGateway notificacionServiceGateway) {
        return new NotificadorReserva(notificacionServiceGateway);
    }

    @Bean
    public ReservaUseCase reservaUseCase(ReservaGateway reservaGateway,
                                         UsuarioServiceGateway usuarioServiceGateway,
                                         CanchaServiceGateway canchaServiceGateway,
                                         NotificadorReserva notificadorReserva,
                                         ValidadorAdmin validadorAdmin,
                                         Clock clock) {
        return new ReservaUseCase(reservaGateway, usuarioServiceGateway, canchaServiceGateway,
                notificadorReserva, validadorAdmin, clock);
    }

    @Bean
    public DisponibilidadUseCase disponibilidadUseCase(ReservaGateway reservaGateway,
                                                       CanchaServiceGateway canchaServiceGateway,
                                                       Clock clock) {
        return new DisponibilidadUseCase(reservaGateway, canchaServiceGateway, clock);
    }

    @Bean
    public PronosticoUseCase pronosticoUseCase(ReservaGateway reservaGateway,
                                               UsuarioServiceGateway usuarioServiceGateway,
                                               CanchaServiceGateway canchaServiceGateway,
                                               ClimaGateway climaGateway,
                                               NotificadorReserva notificadorReserva,
                                               ValidadorAdmin validadorAdmin,
                                               @Value("${reservas.lluvia.umbral}") int umbral) {
        return new PronosticoUseCase(reservaGateway, usuarioServiceGateway, canchaServiceGateway, climaGateway,
                notificadorReserva, validadorAdmin, umbral);
    }

    @Bean
    public RecordatorioUseCase recordatorioUseCase(ReservaGateway reservaGateway,
                                                   UsuarioServiceGateway usuarioServiceGateway,
                                                   CanchaServiceGateway canchaServiceGateway,
                                                   NotificadorReserva notificadorReserva,
                                                   ValidadorAdmin validadorAdmin) {
        return new RecordatorioUseCase(reservaGateway, usuarioServiceGateway, canchaServiceGateway,
                notificadorReserva, validadorAdmin);
    }

    @Bean
    public EstadisticasUseCase estadisticasUseCase(ReservaGateway reservaGateway, ValidadorAdmin validadorAdmin) {
        return new EstadisticasUseCase(reservaGateway, validadorAdmin);
    }
}

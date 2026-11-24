package com.canchas.reservas.domain.model;

import java.time.LocalDate;

public record ResumenAlertaLluvia(LocalDate fecha, int reservasRevisadas, int alertasEnviadas, int sinPronostico) {
}

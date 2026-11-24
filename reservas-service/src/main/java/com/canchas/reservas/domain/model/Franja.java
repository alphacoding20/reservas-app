package com.canchas.reservas.domain.model;

import java.time.LocalTime;

public final class Franja {

    private Franja() {
    }

    public static boolean chocan(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }
}

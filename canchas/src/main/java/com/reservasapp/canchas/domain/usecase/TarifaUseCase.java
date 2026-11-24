package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.Festivo;
import com.reservasapp.canchas.domain.model.Tarifa;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;
import com.reservasapp.canchas.domain.model.gateway.FestivoGateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TarifaUseCase {

    private static final BigDecimal FACTOR_NORMAL = new BigDecimal("1.00");

    private final CanchaGateway canchaGateway;
    private final FestivoGateway festivoGateway;
    private final BigDecimal factorRecargo;

    public TarifaUseCase(CanchaGateway canchaGateway, FestivoGateway festivoGateway, BigDecimal factorRecargo) {
        this.canchaGateway = canchaGateway;
        this.festivoGateway = festivoGateway;
        this.factorRecargo = factorRecargo;
    }

    public Tarifa tarifa(Long canchaId, LocalDate fecha) {
        Cancha cancha = canchaGateway.buscarPorId(canchaId).orElseThrow(NegocioException::canchaNoEncontrada);

        Optional<Festivo> festivo = festivoGateway.festivosDelAnio(fecha.getYear()).stream()
                .filter(f -> f.fecha().equals(fecha))
                .findFirst();
        boolean finDeSemana = fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
        BigDecimal factor = festivo.isPresent() || finDeSemana ? factorRecargo : FACTOR_NORMAL;

        long precioPorHora = BigDecimal.valueOf(cancha.getPrecioPorHora())
                .multiply(factor)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        return new Tarifa(canchaId, fecha, cancha.getPrecioPorHora(), factor, precioPorHora,
                festivo.isPresent(), finDeSemana, festivo.map(Festivo::nombre).orElse(null));
    }

    public List<Festivo> festivos(int anio) {
        return festivoGateway.festivosDelAnio(anio);
    }
}

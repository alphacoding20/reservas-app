package com.reservasapp.canchas.domain.usecase;

import com.reservasapp.canchas.domain.exception.NegocioException;
import com.reservasapp.canchas.domain.model.Cancha;
import com.reservasapp.canchas.domain.model.EstadoCancha;
import com.reservasapp.canchas.domain.model.TipoDeporte;
import com.reservasapp.canchas.domain.model.gateway.BloqueoGateway;
import com.reservasapp.canchas.domain.model.gateway.CanchaGateway;

import java.time.LocalTime;
import java.util.List;

public class CanchaUseCase {

    private static final LocalTime CIERRE_MAXIMO = LocalTime.of(23, 0);

    private final CanchaGateway canchaGateway;
    private final BloqueoGateway bloqueoGateway;

    public CanchaUseCase(CanchaGateway canchaGateway, BloqueoGateway bloqueoGateway) {
        this.canchaGateway = canchaGateway;
        this.bloqueoGateway = bloqueoGateway;
    }

    public Cancha crear(Cancha cancha) {
        validarHorario(cancha.getHoraApertura(), cancha.getHoraCierre());
        cancha.setId(null);
        cancha.setEstado(EstadoCancha.ACTIVA);
        return canchaGateway.guardar(cancha);
    }

    public Cancha obtener(Long id) {
        return canchaGateway.buscarPorId(id).orElseThrow(NegocioException::canchaNoEncontrada);
    }

    public List<Cancha> listar() {
        return canchaGateway.listar();
    }

    public Cancha actualizar(Long id, Cancha datos) {
        obtener(id);
        validarHorario(datos.getHoraApertura(), datos.getHoraCierre());
        datos.setId(id);
        return canchaGateway.guardar(datos);
    }

    public void eliminar(Long id) {
        if (!canchaGateway.existePorId(id)) {
            throw NegocioException.canchaNoEncontrada();
        }
        bloqueoGateway.eliminarPorCancha(id);
        canchaGateway.eliminar(id);
    }

    public List<Cancha> buscar(TipoDeporte deporte, Boolean techada, Long precioMax) {
        return canchaGateway.listarPorEstado(EstadoCancha.ACTIVA).stream()
                .filter(c -> deporte == null || c.getTipoDeporte() == deporte)
                .filter(c -> techada == null || c.isTechada() == techada)
                .filter(c -> precioMax == null || c.getPrecioPorHora() <= precioMax)
                .toList();
    }

    private void validarHorario(LocalTime apertura, LocalTime cierre) {
        if (apertura == null || cierre == null
                || apertura.getMinute() != 0 || cierre.getMinute() != 0
                || !cierre.isAfter(apertura) || cierre.isAfter(CIERRE_MAXIMO)) {
            throw NegocioException.horarioInvalido(
                    "Apertura y cierre deben ser horas en punto, el cierre posterior a la apertura y máximo 23:00");
        }
    }
}

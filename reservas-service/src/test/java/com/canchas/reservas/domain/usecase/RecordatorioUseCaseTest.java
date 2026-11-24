package com.canchas.reservas.domain.usecase;

import com.canchas.reservas.domain.exception.CodigoError;
import com.canchas.reservas.domain.exception.ReservasException;
import com.canchas.reservas.domain.model.DatosNotificacion;
import com.canchas.reservas.domain.model.EstadoReserva;
import com.canchas.reservas.domain.model.ResumenRecordatorios;
import com.canchas.reservas.domain.model.gateway.CanchaServiceGateway;
import com.canchas.reservas.domain.model.gateway.NotificacionServiceGateway;
import com.canchas.reservas.domain.model.gateway.ReservaGateway;
import com.canchas.reservas.domain.model.gateway.UsuarioServiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static com.canchas.reservas.Fixtures.FUTURO;
import static com.canchas.reservas.Fixtures.admin;
import static com.canchas.reservas.Fixtures.assertCodigo;
import static com.canchas.reservas.Fixtures.cancha;
import static com.canchas.reservas.Fixtures.reserva;
import static com.canchas.reservas.Fixtures.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordatorioUseCaseTest {

    @Mock
    ReservaGateway reservas;
    @Mock
    UsuarioServiceGateway usuarios;
    @Mock
    CanchaServiceGateway canchas;
    @Mock
    NotificacionServiceGateway notificaciones;

    RecordatorioUseCase useCase;

    @BeforeEach
    void preparar() {
        useCase = new RecordatorioUseCase(reservas, usuarios, canchas, new NotificadorReserva(notificaciones),
                new ValidadorAdmin(usuarios));
        when(usuarios.validar(1L)).thenReturn(admin());
        when(usuarios.validar(2L)).thenReturn(usuario());
        when(canchas.obtenerCancha(1L)).thenReturn(cancha());
        when(reservas.buscarConfirmadasPorFecha(FUTURO)).thenReturn(List.of(
                reserva(1L, FUTURO, 10, 11, EstadoReserva.CONFIRMADA),
                reserva(2L, FUTURO, 18, 19, EstadoReserva.CONFIRMADA)));
    }

    @Test
    void enviaUnRecordatorioPorReservaYConsultaUsuarioYCanchaUnaSolaVez() {
        ResumenRecordatorios resumen = useCase.enviarManual(1L, FUTURO);

        assertEquals(2, resumen.recordatoriosEnviados());
        assertEquals(FUTURO, resumen.fecha());
        verify(notificaciones, times(2)).enviarRecordatorio(any(DatosNotificacion.class));
        verify(usuarios, times(1)).validar(2L);  // mismo usuario en ambas reservas
        verify(canchas, times(1)).obtenerCancha(1L); // misma cancha en ambas reservas
    }

    @Test
    void omiteLasReservasCuyoUsuarioNoSePudoObtener() {
        when(usuarios.validar(2L)).thenThrow(new ReservasException(CodigoError.USUARIO_NO_ENCONTRADO, "no existe"));

        assertEquals(0, useCase.enviarManual(1L, FUTURO).recordatoriosEnviados());
        verify(notificaciones, never()).enviarRecordatorio(any());
    }

    @Test
    void omiteLasReservasCuyaCanchaNoSePudoObtener() {
        when(canchas.obtenerCancha(1L)).thenThrow(new ReservasException(CodigoError.SERVICIO_NO_DISPONIBLE, "caído"));

        assertEquals(0, useCase.enviarManual(1L, FUTURO).recordatoriosEnviados());
    }

    @Test
    void laEntradaProgramadaNoPideAdmin() {
        assertEquals(2, useCase.enviarParaFecha(FUTURO).recordatoriosEnviados());
        verify(usuarios, never()).validar(1L);
    }

    @Test
    void exigeAdminYFecha() {
        assertCodigo(CodigoError.SIN_CREDENCIALES, () -> useCase.enviarManual(null, FUTURO));
        assertCodigo(CodigoError.PERMISO_DENEGADO, () -> useCase.enviarManual(2L, FUTURO));
        assertCodigo(CodigoError.VALIDACION, () -> useCase.enviarParaFecha(null));
    }
}

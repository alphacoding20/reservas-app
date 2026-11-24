package com.reservasapp.notificaciones.infraestructure.entry_points;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filtro = new CorrelationIdFilter();

    @Test
    void generaUnIdSiLaPeticionNoLoTraeYLoDevuelveEnLaRespuesta() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtro.doFilter(new MockHttpServletRequest(), respuesta, new MockFilterChain());

        assertThat(respuesta.getHeader(CorrelationIdFilter.HEADER)).hasSize(36); // UUID
    }

    @Test
    void conservaElIdValidoQueLlegaDeOtroServicio() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader(CorrelationIdFilter.HEADER, "reserva-55_abc");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtro.doFilter(peticion, respuesta, new MockFilterChain());

        assertThat(respuesta.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("reserva-55_abc");
    }

    @Test
    void reemplazaUnIdConCaracteresPeligrososParaLosLogs() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader(CorrelationIdFilter.HEADER, "malo\nINFO falso");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtro.doFilter(peticion, respuesta, new MockFilterChain());

        assertThat(respuesta.getHeader(CorrelationIdFilter.HEADER)).hasSize(36);
    }

    @Test
    void ponEnElMdcDuranteLaPeticionYLoLimpiaAlTerminar() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader(CorrelationIdFilter.HEADER, "abc-123");
        AtomicReference<String> durante = new AtomicReference<>();

        filtro.doFilter(peticion, new MockHttpServletResponse(), new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
                durante.set(MDC.get(CorrelationIdFilter.MDC_KEY));
            }
        });

        assertThat(durante.get()).isEqualTo("abc-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}

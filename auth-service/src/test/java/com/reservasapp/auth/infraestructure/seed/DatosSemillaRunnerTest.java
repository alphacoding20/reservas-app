package com.reservasapp.auth.infraestructure.seed;

import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosSemillaRunnerTest {

    @Mock
    private UsuarioJpaRepository repository;
    @Mock
    private PasswordEncoder encoder;
    @Captor
    private ArgumentCaptor<List<UsuarioData>> captor;

    @Test
    void run_tablaVacia_creaAdminUsuarioYUsuarioConTelegram() {
        when(repository.count()).thenReturn(0L);
        when(encoder.encode(anyString())).thenAnswer(inv -> "hash-" + inv.getArgument(0));

        new DatosSemillaRunner(repository, encoder, "adm", "usr", "555").run(null);

        verify(repository).saveAll(captor.capture());
        List<UsuarioData> creados = captor.getValue();
        assertEquals(3, creados.size());
        assertEquals("admin@canchas.com", creados.get(0).getCorreo());
        assertEquals(Rol.ADMIN, creados.get(0).getRol());
        assertEquals("hash-adm", creados.get(0).getClave());
        assertEquals(Rol.USUARIO, creados.get(1).getRol());
        assertNull(creados.get(1).getTelegramChatId());
        assertEquals("555", creados.get(2).getTelegramChatId());
        assertTrue(creados.stream().allMatch(UsuarioData::isActivo));
    }

    @Test
    void run_sinChatIdConfigurado_elTercerUsuarioQuedaSinTelegram() {
        when(repository.count()).thenReturn(0L);
        when(encoder.encode(anyString())).thenReturn("hash");

        new DatosSemillaRunner(repository, encoder, "adm", "usr", "").run(null);

        verify(repository).saveAll(captor.capture());
        assertNull(captor.getValue().get(2).getTelegramChatId());
    }

    @Test
    void run_sinChatIdNulo_elTercerUsuarioQuedaSinTelegram() {
        when(repository.count()).thenReturn(0L);
        when(encoder.encode(anyString())).thenReturn("hash");

        new DatosSemillaRunner(repository, encoder, "adm", "usr", null).run(null);

        verify(repository).saveAll(captor.capture());
        assertNull(captor.getValue().get(2).getTelegramChatId());
    }

    @Test
    void run_tablaConDatos_noInsertaNada() {
        when(repository.count()).thenReturn(5L);

        new DatosSemillaRunner(repository, encoder, "adm", "usr", "555").run(null);

        verify(repository, never()).saveAll(any());
        verifyNoInteractions(encoder);
    }
}

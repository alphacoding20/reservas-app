package com.reservasapp.auth.infraestructure.driver_adapters;

import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.domain.model.Usuario;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.BCryptClaveGateway;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioGatewayImpl;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioGatewayImplTest {

    @Mock
    private UsuarioJpaRepository repository;
    @InjectMocks
    private UsuarioGatewayImpl gateway;

    private UsuarioData data() {
        return UsuarioData.builder().id(1L).nombre("Ana").correo("ana@mail.com").clave("hash")
                .rol(Rol.USUARIO).activo(true).build();
    }

    @Test
    void guardar_persisteYDevuelveElDominio() {
        when(repository.save(any(UsuarioData.class))).thenReturn(data());

        Usuario resultado = gateway.guardar(Usuario.builder().nombre("Ana").correo("ana@mail.com")
                .clave("hash").rol(Rol.USUARIO).activo(true).build());

        assertEquals(1L, resultado.getId());
        assertEquals("ana@mail.com", resultado.getCorreo());
    }

    @Test
    void buscarPorId_existente() {
        when(repository.findById(1L)).thenReturn(Optional.of(data()));

        assertEquals("Ana", gateway.buscarPorId(1L).orElseThrow().getNombre());
    }

    @Test
    void buscarPorId_inexistente_devuelveVacio() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorId(1L).isEmpty());
    }

    @Test
    void buscarPorCorreo_existente() {
        when(repository.findByCorreo("ana@mail.com")).thenReturn(Optional.of(data()));

        assertEquals(1L, gateway.buscarPorCorreo("ana@mail.com").orElseThrow().getId());
    }

    @Test
    void buscarPorCorreo_inexistente_devuelveVacio() {
        when(repository.findByCorreo("x@mail.com")).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorCorreo("x@mail.com").isEmpty());
    }

    @Test
    void existePorCorreo_yExistePorId() {
        when(repository.existsByCorreo("ana@mail.com")).thenReturn(true);
        when(repository.existsById(2L)).thenReturn(false);

        assertTrue(gateway.existePorCorreo("ana@mail.com"));
        assertFalse(gateway.existePorId(2L));
    }

    @Test
    void eliminar_delegaEnElRepositorio() {
        gateway.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void bcrypt_codificaYComparaClaves() {
        BCryptClaveGateway claveGateway = new BCryptClaveGateway(new BCryptPasswordEncoder());

        String hash = claveGateway.codificar("secreta1");

        assertNotEquals("secreta1", hash);
        assertTrue(claveGateway.coincide("secreta1", hash));
        assertFalse(claveGateway.coincide("otra", hash));
    }
}

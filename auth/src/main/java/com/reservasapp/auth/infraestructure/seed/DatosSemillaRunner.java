package com.reservasapp.auth.infraestructure.seed;

import com.reservasapp.auth.domain.model.Rol;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.reservasapp.auth.infraestructure.driver_adapters.jpa_repository.UsuarioJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DatosSemillaRunner implements ApplicationRunner {

    private final UsuarioJpaRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String claveAdmin;
    private final String claveUsuario;
    private final String telegramChatId;

    public DatosSemillaRunner(UsuarioJpaRepository repository,
                              PasswordEncoder passwordEncoder,
                              @Value("${seed.admin.clave}") String claveAdmin,
                              @Value("${seed.usuario.clave}") String claveUsuario,
                              @Value("${seed.telegram.chat-id:}") String telegramChatId) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.claveAdmin = claveAdmin;
        this.claveUsuario = claveUsuario;
        this.telegramChatId = telegramChatId;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        UsuarioData admin = UsuarioData.builder()
                .nombre("Administrador")
                .correo("admin@canchas.com")
                .clave(passwordEncoder.encode(claveAdmin))
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        UsuarioData usuarioPrueba = UsuarioData.builder()
                .nombre("Usuario de Prueba")
                .correo("usuario@canchas.com")
                .clave(passwordEncoder.encode(claveUsuario))
                .rol(Rol.USUARIO)
                .activo(true)
                .build();
        UsuarioData usuarioTelegram = UsuarioData.builder()
                .nombre("Usuario Telegram")
                .correo("telegram@canchas.com")
                .clave(passwordEncoder.encode(claveUsuario))
                .rol(Rol.USUARIO)
                .activo(true)
                .telegramChatId(telegramChatId == null || telegramChatId.isBlank() ? null : telegramChatId)
                .build();
        repository.saveAll(List.of(admin, usuarioPrueba, usuarioTelegram));
        log.info("Datos semilla de usuarios creados (ids 1, 2 y 3)");
    }
}

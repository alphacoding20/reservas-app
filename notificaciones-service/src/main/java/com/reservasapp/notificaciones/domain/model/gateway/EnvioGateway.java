package com.reservasapp.notificaciones.domain.model.gateway;

import com.reservasapp.notificaciones.domain.model.Canal;
import com.reservasapp.notificaciones.domain.model.ResultadoEnvio;

/**
 * Puerto de salida para enviar un mensaje por un canal. Agregar correo = una nueva implementacion
 * que responda {@code soporta(Canal.EMAIL)}; ningun caso de uso cambia (abierto/cerrado).
 */
public interface EnvioGateway {

    boolean soporta(Canal canal);

    ResultadoEnvio enviar(Canal canal, String destinatario, String mensaje);
}

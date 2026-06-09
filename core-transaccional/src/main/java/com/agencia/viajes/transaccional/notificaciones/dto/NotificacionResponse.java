package com.agencia.viajes.transaccional.notificaciones.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Respuesta GraphQL de una notificación.
 */
@Value
@Builder
public class NotificacionResponse {
    Integer id;
    Integer idUsuario;
    String tipo;
    String titulo;
    String mensaje;
    String fechaCreacion;
    Boolean leido;
    String datosExtraJson;
}

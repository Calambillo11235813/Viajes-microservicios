package com.agencia.viajes.transaccional.notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input GraphQL para notificar pasajeros de un viaje.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionViajeInput {
    private Integer idViaje;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String datosExtraJson;
}

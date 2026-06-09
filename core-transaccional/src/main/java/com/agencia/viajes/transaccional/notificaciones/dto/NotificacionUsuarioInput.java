package com.agencia.viajes.transaccional.notificaciones.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input GraphQL para notificar usuarios específicos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionUsuarioInput {
    private List<Integer> idsUsuario;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String datosExtraJson;
}

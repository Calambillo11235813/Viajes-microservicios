package com.agencia.viajes.transaccional.notificaciones.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Página paginada de notificaciones con contador de no leídas.
 */
@Getter
@AllArgsConstructor
public class PaginaNotificacionesResponse {
    private final List<NotificacionResponse> contenido;
    private final int totalPaginas;
    private final long totalElementos;
    private final int paginaActual;
    private final boolean tieneSiguiente;
    private final long totalNoLeidas;
}

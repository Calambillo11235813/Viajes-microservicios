package com.agencia.viajes.transaccional.reservas.dto;

import java.util.List;

/**
 * DTO paginado para historial de viajes.
 */
public record PaginaHistorialResponse(
        List<HistorialViajeResponse> contenido,
        int totalPaginas,
        long totalElementos,
        int paginaActual,
        boolean tieneSiguiente) {
}

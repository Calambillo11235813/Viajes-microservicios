package com.agencia.viajes.transaccional.viajes.dto;

import java.util.List;

/**
 * DTO paginado para resultados de viajes.
 */
public record PaginaViajesResponse(
        List<ViajeDisponibleResponse> contenido,
        int totalPaginas,
        long totalElementos,
        int paginaActual,
        boolean tieneSiguiente) {
}

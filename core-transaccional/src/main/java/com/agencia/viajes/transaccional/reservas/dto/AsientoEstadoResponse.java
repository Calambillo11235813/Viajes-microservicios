package com.agencia.viajes.transaccional.reservas.dto;

/**
 * Estado de un asiento dentro del mapa de un viaje programado.
 */
public record AsientoEstadoResponse(
        String numeroAsiento,
        Boolean ocupado) {
}

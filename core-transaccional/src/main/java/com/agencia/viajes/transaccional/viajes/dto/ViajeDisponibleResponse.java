package com.agencia.viajes.transaccional.viajes.dto;

import java.math.BigDecimal;

/**
 * Datos visibles para el cliente al buscar rutas y horarios disponibles.
 */
public record ViajeDisponibleResponse(
        Integer idViaje,
        Integer idRuta,
        String ciudadOrigen,
        String ciudadDestino,
        String fechaHoraSalida,
        String fechaHoraLlegada,
        BigDecimal duracionEstimadaHoras,
        BigDecimal precioBase,
        String categoriaTuristica,
        Integer idBus,
        String tipoBus,
        Integer capacidadTotalAsientos,
        String estadoViaje) {
}

package com.agencia.viajes.transaccional.navegacion.clicks.dto;

/**
 * Datos de entrada para registrar un click o visualización en {@code ClicksViajero}.
 */
public record ClickViajeroRequest(
        Integer idUsuario,
        String tipoEvento,
        Integer idRuta,
        Integer tiempoPermanenciaSeg,
        String canal,
        String dispositivo) {
}

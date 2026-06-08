package com.agencia.viajes.transaccional.navegacion.dto;

/**
 * Datos de entrada para registrar una visualización de ruta en DynamoDB (CU-13).
 */
public record VisualizacionRutaRequest(
        Integer idUsuario,
        Integer idRuta,
        String origen,
        String destino,
        String canal,
        String categoriaVista,
        String ciudadOrigenVista,
        String ciudadDestinoVista,
        Integer idRutaVista,
        Integer tiempoPermanenciaSeg,
        String dispositivo) {
}

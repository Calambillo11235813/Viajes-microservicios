package com.agencia.viajes.transaccional.navegacion.feedback.dto;

/**
 * Datos de entrada para registrar feedback post-viaje en {@code FeedbackViajero}.
 */
public record FeedbackViajeroRequest(
        Integer idUsuario,
        Integer idViaje,
        Integer idReserva,
        Integer calificacion,
        String comentario) {
}

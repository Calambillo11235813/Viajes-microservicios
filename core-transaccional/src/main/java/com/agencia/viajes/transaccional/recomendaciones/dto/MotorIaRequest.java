package com.agencia.viajes.transaccional.recomendaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cuerpo de la petición REST enviada al motor IA (microservicio Django) para
 * obtener la recomendación de ruta. Los nombres JSON usan snake_case para
 * coincidir con el contrato del endpoint {@code /api/v1/recomendar-ruta/}.
 *
 * @param perfilPasajero perfil del usuario (Económico, Estándar, Premium).
 * @param categoriaPreferida categoría turística preferida del usuario.
 * @param montoTotalPagado presupuesto o gasto histórico usado como característica.
 * @param cantidadPasajeros número de pasajeros (fijo en 1 por ahora).
 */
public record MotorIaRequest(
        @JsonProperty("perfil_pasajero") String perfilPasajero,
        @JsonProperty("categoria_preferida") String categoriaPreferida,
        @JsonProperty("monto_total_pagado") Double montoTotalPagado,
        @JsonProperty("cantidad_pasajeros") Integer cantidadPasajeros) {
}

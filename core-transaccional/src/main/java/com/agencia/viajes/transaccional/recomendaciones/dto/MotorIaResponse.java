package com.agencia.viajes.transaccional.recomendaciones.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Respuesta REST devuelta por el motor IA (microservicio Django). Se ignoran las
 * propiedades desconocidas (como {@code status} o {@code modulo}) para mantener el
 * acoplamiento mínimo con el contrato del modelo.
 *
 * @param prediccionIdRuta identificador de la ruta predicha como principal.
 * @param top3Rutas lista de identificadores de las rutas más probables.
 * @param top3Probabilidades lista de probabilidades alineada con {@code top3Rutas}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MotorIaResponse(
        @JsonProperty("prediccion_id_ruta") Integer prediccionIdRuta,
        @JsonProperty("top_3_rutas") List<Integer> top3Rutas,
        @JsonProperty("top_3_probabilidades") List<Double> top3Probabilidades) {
}

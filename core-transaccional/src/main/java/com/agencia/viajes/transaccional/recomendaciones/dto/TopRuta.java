package com.agencia.viajes.transaccional.recomendaciones.dto;

/**
 * Representa una de las rutas candidatas devueltas por el motor IA,
 * junto con la probabilidad estimada por el modelo.
 *
 * @param idRuta identificador de la ruta sugerida.
 * @param probabilidad probabilidad asociada a la ruta (0.0 a 1.0).
 */
public record TopRuta(
        Integer idRuta,
        Double probabilidad) {
}

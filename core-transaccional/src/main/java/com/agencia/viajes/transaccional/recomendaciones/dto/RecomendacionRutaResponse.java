package com.agencia.viajes.transaccional.recomendaciones.dto;

import java.util.List;

/**
 * Respuesta de la recomendación personalizada de rutas (CU-09) entregada al cliente.
 * Incluye la ruta principal sugerida, el perfil y categoría calculados, el top de
 * rutas candidatas y un mensaje de advertencia cuando se devuelve un resultado por
 * defecto (por ejemplo, si el motor IA no respondió).
 *
 * @param rutaRecomendadaId identificador de la ruta recomendada principal.
 * @param perfilUsuario perfil del pasajero calculado (Económico, Estándar, Premium).
 * @param categoriaPreferida categoría turística preferida del usuario o "Desconocido".
 * @param topRutas lista de hasta 3 rutas candidatas con su probabilidad.
 * @param advertencia mensaje informativo cuando se usa un valor por defecto; nulo si todo fue exitoso.
 */
public record RecomendacionRutaResponse(
        Integer rutaRecomendadaId,
        String perfilUsuario,
        String categoriaPreferida,
        List<TopRuta> topRutas,
        String advertencia) {
}

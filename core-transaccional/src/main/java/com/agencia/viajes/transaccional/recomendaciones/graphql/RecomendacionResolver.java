package com.agencia.viajes.transaccional.recomendaciones.graphql;

import com.agencia.viajes.transaccional.recomendaciones.dto.RecomendacionRutaResponse;
import com.agencia.viajes.transaccional.recomendaciones.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolver GraphQL para la recomendación personalizada de rutas (CU-09).
 */
@Controller
@RequiredArgsConstructor
public class RecomendacionResolver {

    private final RecomendacionService recomendacionService;

    /**
     * Consulta GraphQL que devuelve la ruta recomendada para un usuario, junto con
     * su perfil, categoría preferida y el top de rutas candidatas.
     *
     * @param idUsuario identificador del usuario (obligatorio).
     * @param presupuesto presupuesto opcional; si es nulo se usa el gasto histórico.
     * @return recomendación de ruta personalizada.
     */
    @QueryMapping
    public RecomendacionRutaResponse obtenerRecomendacionRuta(
            @Argument Integer idUsuario,
            @Argument Double presupuesto) {
        return recomendacionService.recomendarRuta(idUsuario, presupuesto);
    }
}

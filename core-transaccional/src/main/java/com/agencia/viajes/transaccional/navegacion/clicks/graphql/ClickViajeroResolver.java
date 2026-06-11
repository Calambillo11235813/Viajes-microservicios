package com.agencia.viajes.transaccional.navegacion.clicks.graphql;

import com.agencia.viajes.transaccional.navegacion.clicks.service.ClickViajeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Mutaciones GraphQL para registro de clicks/visualizaciones en {@code ClicksViajero}.
 */
@Controller
@RequiredArgsConstructor
public class ClickViajeroResolver {

    private final ClickViajeroService clickViajeroService;

    /**
     * Registra la visualización de una ruta (compatibilidad con app móvil CU-13).
     *
     * @return {@code true} si la petición fue aceptada para registro.
     */
    @MutationMapping
    public boolean registrarVisualizacionRuta(
            @Argument Integer idUsuario,
            @Argument Integer idRuta,
            @Argument String origen,
            @Argument String destino,
            @Argument String canal,
            @Argument String categoriaVista,
            @Argument String ciudadOrigenVista,
            @Argument String ciudadDestinoVista,
            @Argument Integer idRutaVista,
            @Argument Integer tiempoPermanenciaSeg,
            @Argument String dispositivo) {
        clickViajeroService.registrarVisualizacion(
                idUsuario,
                idRuta,
                idRutaVista,
                canal,
                tiempoPermanenciaSeg,
                dispositivo);
        return true;
    }
}

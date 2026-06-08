package com.agencia.viajes.transaccional.navegacion.graphql;

import com.agencia.viajes.transaccional.navegacion.dto.VisualizacionRutaRequest;
import com.agencia.viajes.transaccional.navegacion.service.NavegacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Mutaciones GraphQL para registro de eventos de navegación en DynamoDB.
 */
@Controller
@RequiredArgsConstructor
public class NavegacionResolver {

    private final NavegacionService navegacionService;

    /**
     * Registra la visualización de una ruta por un usuario autenticado o identificado.
     *
     * @param idUsuario identificador del usuario.
     * @param idRuta identificador de la ruta visualizada (opcional).
     * @param origen ciudad de origen (opcional).
     * @param destino ciudad de destino (opcional).
     * @param canal canal de origen del evento (opcional).
     * @param categoriaVista categoría turística de la ruta visualizada (opcional).
     * @param ciudadOrigenVista ciudad de origen de la vista (opcional).
     * @param ciudadDestinoVista ciudad de destino de la vista (opcional).
     * @param idRutaVista identificador de ruta para análisis BI/IA (opcional).
     * @param tiempoPermanenciaSeg tiempo de permanencia en segundos (opcional).
     * @param dispositivo tipo de dispositivo, p. ej. ios/android (opcional).
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
        navegacionService.registrarVisualizacion(new VisualizacionRutaRequest(
                idUsuario,
                idRuta,
                origen,
                destino,
                canal,
                categoriaVista,
                ciudadOrigenVista,
                ciudadDestinoVista,
                idRutaVista,
                tiempoPermanenciaSeg,
                dispositivo));
        return true;
    }
}

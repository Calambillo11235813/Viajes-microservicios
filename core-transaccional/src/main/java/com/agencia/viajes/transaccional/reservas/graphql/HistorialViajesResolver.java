package com.agencia.viajes.transaccional.reservas.graphql;

import com.agencia.viajes.transaccional.reservas.dto.HistorialViajeResponse;
import com.agencia.viajes.transaccional.reservas.service.HistorialViajesService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador de GraphQL para el historial de viajes.
 */
@Controller
@RequiredArgsConstructor
public class HistorialViajesResolver {

    private final HistorialViajesService historialViajesService;

    @QueryMapping
    public List<HistorialViajeResponse> consultarHistorialViajes(@Argument Integer idUsuario) {
        return historialViajesService.obtenerHistorialViajes(idUsuario);
    }
}

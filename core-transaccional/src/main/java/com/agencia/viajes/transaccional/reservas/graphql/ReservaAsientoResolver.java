package com.agencia.viajes.transaccional.reservas.graphql;

import com.agencia.viajes.transaccional.reservas.dto.AsientoEstadoResponse;
import com.agencia.viajes.transaccional.reservas.dto.ReservaProvisionalResponse;
import com.agencia.viajes.transaccional.reservas.service.ReservaAsientoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Operaciones GraphQL para selección de asiento y reserva provisional.
 */
@Controller
@RequiredArgsConstructor
public class ReservaAsientoResolver {

    private final ReservaAsientoService reservaAsientoService;

    /**
     * Obtiene la ocupación de asientos de un viaje programado.
     *
     * @param idViaje identificador del viaje programado.
     * @return mapa simple de asientos ocupados y disponibles.
     */
    @QueryMapping
    public List<AsientoEstadoResponse> obtenerMapaAsientos(@Argument Integer idViaje) {
        return reservaAsientoService.obtenerMapaAsientos(idViaje);
    }

    /**
     * Crea una reserva provisional para el asiento seleccionado.
     *
     * @param idUsuario identificador del cliente.
     * @param idViaje identificador del viaje programado.
     * @param numeroAsiento asiento solicitado.
     * @param nombrePasajero nombre del pasajero.
     * @param tipoPasajero tipo de pasajero.
     * @return reserva provisional creada.
     */
    @MutationMapping
    public ReservaProvisionalResponse seleccionarAsientoYReservar(
            @Argument Integer idUsuario,
            @Argument Integer idViaje,
            @Argument String numeroAsiento,
            @Argument String nombrePasajero,
            @Argument String tipoPasajero) {
        return reservaAsientoService.seleccionarAsientoYReservar(
                idUsuario,
                idViaje,
                numeroAsiento,
                nombrePasajero,
                tipoPasajero);
    }
}

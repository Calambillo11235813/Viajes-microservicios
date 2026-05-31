package com.agencia.viajes.transaccional.reservas.graphql;

import com.agencia.viajes.transaccional.reservas.dto.ReservaCanceladaResponse;
import com.agencia.viajes.transaccional.reservas.service.CancelacionReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Operaciones GraphQL para la cancelación de reservas (CU-06).
 */
@Controller
@RequiredArgsConstructor
public class CancelacionReservaResolver {

    private final CancelacionReservaService cancelacionReservaService;

    /**
     * Cancela una reserva activa, libera los asientos y reembolsa el pago si corresponde.
     *
     * @param idReserva identificador de la reserva a cancelar.
     * @param idUsuario identificador del usuario propietario de la reserva.
     * @return datos de la cancelación realizada.
     */
    @MutationMapping
    public ReservaCanceladaResponse cancelarReserva(
            @Argument Integer idReserva,
            @Argument Integer idUsuario) {
        return cancelacionReservaService.cancelarReserva(idReserva, idUsuario);
    }
}

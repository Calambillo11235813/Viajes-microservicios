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
            @Argument Integer idUsuario,
            graphql.GraphQLContext context) {
        
        com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");
        if (!usuario.getIdUsuario().equals(idUsuario) && !"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("No tienes permiso para cancelar reservas de otro usuario");
        }

        return cancelacionReservaService.cancelarReserva(idReserva, idUsuario);
    }
}

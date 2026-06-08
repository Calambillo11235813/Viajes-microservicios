package com.agencia.viajes.transaccional.pagos.graphql;

import com.agencia.viajes.transaccional.pagos.dto.PagoConfirmadoResponse;
import com.agencia.viajes.transaccional.pagos.service.PagoService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Operaciones GraphQL para pagos de reservas.
 */
@Controller
@RequiredArgsConstructor
public class PagoResolver {

    private final PagoService pagoService;

    /**
     * Registra un pago acreditado por QR o transferencia y confirma la reserva.
     *
     * @param idReserva reserva pendiente.
     * @param metodoPagoUsado QR o TRANSFERENCIA.
     * @param montoTransaccion monto acreditado.
     * @param acreditado verificación externa del pago.
     * @param cuponDescuentoAplicado cupón opcional.
     * @return pago confirmado.
     */
    @MutationMapping
    public PagoConfirmadoResponse realizarPago(
            @Argument Integer idReserva,
            @Argument String metodoPagoUsado,
            @Argument Double montoTransaccion,
            @Argument Boolean acreditado,
            @Argument String cuponDescuentoAplicado,
            graphql.GraphQLContext context) throws Exception {

        com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");

        return pagoService.realizarPago(
                idReserva,
                metodoPagoUsado,
                montoTransaccion == null ? null : BigDecimal.valueOf(montoTransaccion),
                acreditado,
                cuponDescuentoAplicado);
    }
}

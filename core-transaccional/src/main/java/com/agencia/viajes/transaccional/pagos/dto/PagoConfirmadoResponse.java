package com.agencia.viajes.transaccional.pagos.dto;

import java.math.BigDecimal;

/**
 * Respuesta de confirmación de pago acreditado.
 */
public record PagoConfirmadoResponse(
        Integer idPago,
        Integer idReserva,
        Integer idUsuario,
        Integer idViaje,
        BigDecimal montoTransaccion,
        String metodoPagoUsado,
        String estadoPago,
        String estadoReserva,
        String fechaPago,
        Boolean eventoEmitido) {
}

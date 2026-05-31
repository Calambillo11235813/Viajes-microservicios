package com.agencia.viajes.transaccional.reservas.dto;

import java.math.BigDecimal;

/**
 * Respuesta tras la cancelación exitosa de una reserva.
 * Incluye el estado final de la reserva, los boletos y el pago asociado.
 */
public record ReservaCanceladaResponse(
        Integer idReserva,
        Integer idUsuario,
        Integer idViaje,
        String ciudadOrigen,
        String ciudadDestino,
        String estadoReserva,
        String estadoPago,
        Integer boletosAnulados,
        BigDecimal montoReembolsado,
        String fechaCancelacion) {
}

package com.agencia.viajes.transaccional.reservas.dto;

import java.math.BigDecimal;

/**
 * Resultado de una reserva provisional de asiento.
 */
public record ReservaProvisionalResponse(
        Integer idReserva,
        Integer idBoleto,
        Integer idViaje,
        Integer idUsuario,
        String numeroAsiento,
        String nombrePasajero,
        String tipoPasajero,
        String estadoReserva,
        String estadoBoleto,
        BigDecimal montoEstimado,
        String fechaCreacion) {
}

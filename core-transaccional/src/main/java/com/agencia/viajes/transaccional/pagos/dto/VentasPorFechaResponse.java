package com.agencia.viajes.transaccional.pagos.dto;

import java.math.BigDecimal;

/**
 * Representa las ventas acumuladas en un día específico.
 */
public record VentasPorFechaResponse(
        String fecha,
        BigDecimal montoDia,
        Integer cantidadPagosDia
) {}

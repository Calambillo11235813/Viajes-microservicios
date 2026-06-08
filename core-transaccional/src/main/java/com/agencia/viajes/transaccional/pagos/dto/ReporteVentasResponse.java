package com.agencia.viajes.transaccional.pagos.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporte consolidado de ventas en un rango de fechas determinado.
 */
public record ReporteVentasResponse(
        BigDecimal montoTotal,
        Integer cantidadPagos,
        Double ocupacionFlota,
        String fechaInicio,
        String fechaFin,
        List<VentasPorFechaResponse> detallesPorFecha
) {}

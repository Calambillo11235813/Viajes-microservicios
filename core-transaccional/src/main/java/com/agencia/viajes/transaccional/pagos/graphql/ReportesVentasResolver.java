package com.agencia.viajes.transaccional.pagos.graphql;

import com.agencia.viajes.transaccional.pagos.dto.ReporteVentasResponse;
import com.agencia.viajes.transaccional.pagos.service.ReportesVentasService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolver GraphQL para reportes de ventas (CU-10).
 */
@Controller
@RequiredArgsConstructor
public class ReportesVentasResolver {

    private final ReportesVentasService reportesVentasService;

    /**
     * Consulta GraphQL para generar reportes de ventas agrupadas por fecha en un rango dado.
     *
     * @param fechaInicio fecha de inicio.
     * @param fechaFin fecha de fin.
     * @return reporte consolidado de ventas.
     */
    @QueryMapping
    public ReporteVentasResponse generarReporteVentas(
            @Argument String fechaInicio,
            @Argument String fechaFin) {
        return reportesVentasService.generarReporteVentas(fechaInicio, fechaFin);
    }
}

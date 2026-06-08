package com.agencia.viajes.transaccional.pagos.graphql;

import com.agencia.viajes.transaccional.pagos.dto.ReporteVentasResponse;
import com.agencia.viajes.transaccional.pagos.service.ReportesVentasService;
import lombok.RequiredArgsConstructor;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
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
    private void verificarGerenteOAdmin(GraphQLContext context) {
        UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");
        if (!"ADMINISTRADOR".equals(usuario.getRol()) && !"GERENTE".equals(usuario.getRol())) {
            throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Acceso denegado: Se requiere rol GERENTE o ADMINISTRADOR");
        }
    }

    @QueryMapping
    public ReporteVentasResponse generarReporteVentas(
            @Argument String fechaInicio,
            @Argument String fechaFin,
            GraphQLContext context) {
        verificarGerenteOAdmin(context);
        return reportesVentasService.generarReporteVentas(fechaInicio, fechaFin);
    }
}

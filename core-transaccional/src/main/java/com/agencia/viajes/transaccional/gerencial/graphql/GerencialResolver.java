package com.agencia.viajes.transaccional.gerencial.graphql;

import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.EvolucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse;
import com.agencia.viajes.transaccional.gerencial.dto.MapaRutasComplementariasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida;
import com.agencia.viajes.transaccional.gerencial.dto.RutasPorClusterResponse;
import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GerencialResolver {

    private final GerencialService gerencialService;

    private void verificarGerente(GraphQLContext context) {
        UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null || !"GERENTE".equals(usuario.getRol())) {
            throw new SecurityException("Acceso denegado: Se requiere rol GERENTE");
        }
    }

    @QueryMapping
    public KpisGeneralesResponse kpisGenerales(GraphQLContext context) {
        verificarGerente(context);
        return gerencialService.obtenerKpisGenerales();
    }

    @QueryMapping
    public List<ReglaAsociacionEnriquecida> reglasAsociacion(
            @Argument Integer top,
            @Argument String ordenarPor,
            GraphQLContext context) {
        verificarGerente(context);

        String order = ordenarPor != null ? ordenarPor.toLowerCase() : "lift";
        if (!order.equals("lift") && !order.equals("confidence") && !order.equals("support")) {
            throw new IllegalArgumentException("El parámetro ordenarPor solo acepta: lift, confidence o support");
        }

        return gerencialService.obtenerReglasAsociacion(top != null ? top : 20, order);
    }

    @QueryMapping
    public DistribucionClustersResponse distribucionClusters(GraphQLContext context) {
        verificarGerente(context);
        return gerencialService.obtenerDistribucionClusters();
    }

    @QueryMapping
    public EvolucionClustersResponse evolucionClusters(
            @Argument String fechaInicio,
            @Argument String fechaFin,
            @Argument String intervalo,
            GraphQLContext context) {
        verificarGerente(context);

        try {
            LocalDate fInicio = LocalDate.parse(fechaInicio);
            LocalDate fFin = LocalDate.parse(fechaFin);
            if (fInicio.isAfter(fFin)) {
                throw new IllegalArgumentException("La fechaInicio no puede ser posterior a fechaFin");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Las fechas deben venir en formato YYYY-MM-DD");
        }

        return gerencialService.obtenerEvolucionClusters(fechaInicio, fechaFin, intervalo != null ? intervalo : "MENSUAL");
    }

    @QueryMapping
    public MapaRutasComplementariasResponse mapaRutasComplementarias(GraphQLContext context) {
        verificarGerente(context);
        return gerencialService.obtenerMapaRutasComplementarias();
    }

    @QueryMapping
    public RutasPorClusterResponse rutasPorCluster(
            @Argument Integer clusterId,
            GraphQLContext context) {
        verificarGerente(context);
        if (clusterId == null) {
            throw new IllegalArgumentException("El clusterId es requerido");
        }
        return gerencialService.obtenerRutasPorCluster(clusterId);
    }
}

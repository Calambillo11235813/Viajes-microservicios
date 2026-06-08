package com.agencia.viajes.transaccional.gerencial.controller;

import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.EvolucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse;
import com.agencia.viajes.transaccional.gerencial.dto.MapaRutasComplementariasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida;
import com.agencia.viajes.transaccional.gerencial.dto.RutasPorClusterResponse;
import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gerencial")
@RequiredArgsConstructor
public class GerencialController {

    private final GerencialService gerencialService;

    @GetMapping("/kpis-generales")
    public ResponseEntity<KpisGeneralesResponse> obtenerKpisGenerales() {
        return ResponseEntity.ok(gerencialService.obtenerKpisGenerales());
    }

    @GetMapping("/reglas-asociacion")
    public ResponseEntity<List<ReglaAsociacionEnriquecida>> obtenerReglasAsociacion(
            @RequestParam(defaultValue = "20") int top,
            @RequestParam(defaultValue = "lift") String ordenarPor) {
        return ResponseEntity.ok(gerencialService.obtenerReglasAsociacion(top, ordenarPor));
    }

    @GetMapping("/distribucion-clusters")
    public ResponseEntity<DistribucionClustersResponse> obtenerDistribucionClusters() {
        return ResponseEntity.ok(gerencialService.obtenerDistribucionClusters());
    }

    @GetMapping("/evolucion-clusters")
    public ResponseEntity<EvolucionClustersResponse> obtenerEvolucionClusters(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String intervalo) {
        return ResponseEntity.ok(gerencialService.obtenerEvolucionClusters(fechaInicio, fechaFin, intervalo));
    }

    @GetMapping("/mapa-rutas-complementarias")
    public ResponseEntity<MapaRutasComplementariasResponse> obtenerMapaRutasComplementarias() {
        return ResponseEntity.ok(gerencialService.obtenerMapaRutasComplementarias());
    }

    @GetMapping("/rutas-por-cluster")
    public ResponseEntity<RutasPorClusterResponse> obtenerRutasPorCluster(
            @RequestParam int clusterId) {
        return ResponseEntity.ok(gerencialService.obtenerRutasPorCluster(clusterId));
    }
}

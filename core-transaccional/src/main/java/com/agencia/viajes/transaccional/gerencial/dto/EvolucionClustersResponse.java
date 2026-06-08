package com.agencia.viajes.transaccional.gerencial.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvolucionClustersResponse {
    private String fechaInicio;
    private String fechaFin;
    private String intervalo;
    private List<PuntoSerie> serie;
    private Map<String, Integer> migraciones;

    @Data
    @Builder
    public static class PuntoSerie {
        private String fecha;
        private List<ClusterCantidad> clusters;
    }

    @Data
    @Builder
    public static class ClusterCantidad {
        private Integer clusterId;
        private Integer cantidad;
        private Double porcentaje;
    }
}

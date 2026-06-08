package com.agencia.viajes.transaccional.gerencial.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistribucionClustersResponse {
    private LocalDateTime fechaUltimaSegmentacion;
    private Integer nClusters;
    private List<ClusterDetalle> clusters;

    @Data
    @Builder
    public static class ClusterDetalle {
        private Integer clusterId;
        private String etiqueta;
        private Integer cantidadUsuarios;
        private Double porcentaje;
        private Centroide centroide;
        private Metricas metricas;
    }

    @Data
    @Builder
    public static class Centroide {
        private Double totalGastado;
        private Integer numReservas;
        private Integer rutasDistintas;
        private Double promedioPasajeros;
    }

    @Data
    @Builder
    public static class Metricas {
        private Double ingresoTotal;
        private Double ingresoPromedio;
        private Double tasaConversion;
        private Double ticketPromedio;
    }
}

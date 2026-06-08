package com.agencia.viajes.transaccional.gerencial.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KpisGeneralesResponse {
    private LocalDate fechaSnapshot;
    private SegmentacionResumen segmentacion;
    private ReglasAsociacionResumen reglasAsociacion;

    @Data
    @Builder
    public static class SegmentacionResumen {
        private Integer totalUsuarios;
        private Integer totalSegmentados;
        private List<ClusterResumen> clusters;
        private List<ConversionCluster> conversionPorCluster;
    }

    @Data
    @Builder
    public static class ClusterResumen {
        private Integer clusterId;
        private String etiqueta;
        private Integer cantidad;
        private Double porcentaje;
        private Double ingresoPromedio;
    }

    @Data
    @Builder
    public static class ConversionCluster {
        private Integer clusterId;
        private Double tasaConversion;
    }

    @Data
    @Builder
    public static class ReglasAsociacionResumen {
        private Integer totalReglas;
        private Integer reglasAltoLift;
        private Double supportPromedioTop20;
        private Integer indiceCrossSelling;
    }
}

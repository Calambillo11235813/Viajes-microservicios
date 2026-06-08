package com.agencia.viajes.transaccional.gerencial.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RutasPorClusterResponse {
    private Integer clusterId;
    private String etiqueta;
    private List<RutaFrecuente> rutasFrecuentes;
    private List<ReglaAsociacionEnriquecida> reglasRelevantes;

    @Data
    @Builder
    public static class RutaFrecuente {
        private Integer idRuta;
        private String descripcion;
        private Long frecuencia;
        private BigDecimal ingresoTotal;
    }
}

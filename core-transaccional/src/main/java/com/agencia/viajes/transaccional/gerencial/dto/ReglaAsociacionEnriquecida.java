package com.agencia.viajes.transaccional.gerencial.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReglaAsociacionEnriquecida {
    private List<RutaBasica> antecedentes;
    private RutaBasica consecuente;
    private BigDecimal soporte;
    private BigDecimal confianza;
    private BigDecimal lift;
    private String interpretacion;

    @Data
    @Builder
    public static class RutaBasica {
        private Integer idRuta;
        private String descripcion;
    }
}

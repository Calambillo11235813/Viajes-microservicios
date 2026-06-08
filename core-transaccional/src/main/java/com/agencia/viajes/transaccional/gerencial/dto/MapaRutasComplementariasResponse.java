package com.agencia.viajes.transaccional.gerencial.dto;

import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida.RutaBasica;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MapaRutasComplementariasResponse {
    private List<RutaBasica> rutas;
    private List<CoOcurrencia> matriz;

    @Data
    @Builder
    public static class CoOcurrencia {
        private Integer rutaOrigen;
        private Integer rutaDestino;
        private BigDecimal lift;
        private BigDecimal confianza;
    }
}

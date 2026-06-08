package com.agencia.viajes.transaccional.gerencial.dto.ia;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CU10ReglasResponse {
    private String status;
    private Integer total;
    private List<ReglaDTO> reglas;

    @Data
    public static class ReglaDTO {
        private List<Integer> antecedents;
        private Integer consequents;
        private BigDecimal support;
        private BigDecimal confidence;
        private BigDecimal lift;
    }
}

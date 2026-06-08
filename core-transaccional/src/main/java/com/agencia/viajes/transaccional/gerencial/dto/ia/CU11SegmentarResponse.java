package com.agencia.viajes.transaccional.gerencial.dto.ia;

import java.util.Map;
import lombok.Data;

@Data
public class CU11SegmentarResponse {
    private String status;
    private Integer cluster;
    private Map<String, Object> caracteristicas;
    private String error;
}

package com.agencia.viajes.transaccional.gerencial.dto.ia;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CU11EstadisticasResponse {
    private String status;
    private Integer n_clusters;
    private List<ClusterInfoDTO> clusters;
    private String mensaje;

    @Data
    public static class ClusterInfoDTO {
        private Integer cluster;
        private CentroideDTO centroide;
    }

    @Data
    public static class CentroideDTO {
        private BigDecimal total_gastado;
        private Integer num_reservas;
        private Integer rutas_distintas;
        private BigDecimal promedio_pasajeros;
    }
}

package com.agencia.viajes.transaccional.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinoRecomendadoResponse {
    private String ciudad;
    private Double porcentajeCoincidencia;
    private String descripcion;
}

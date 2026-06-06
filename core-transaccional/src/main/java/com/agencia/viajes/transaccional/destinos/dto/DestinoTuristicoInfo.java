package com.agencia.viajes.transaccional.destinos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinoTuristicoInfo {
    private Integer id;
    private String nombreTuristico;
    private String departamento;
    private String descripcion;
}

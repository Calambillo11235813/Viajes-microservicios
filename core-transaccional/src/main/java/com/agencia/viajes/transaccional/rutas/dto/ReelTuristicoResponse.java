package com.agencia.viajes.transaccional.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReelTuristicoResponse {
    private String urlVideo;
    private Integer duracionSegundos;
}

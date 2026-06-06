package com.agencia.viajes.transaccional.destinos.dto;

import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinoViajesResponse {
    private DestinoTuristicoInfo destino;
    private List<ViajeDisponibleResponse> viajesDisponibles;
}

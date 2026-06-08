package com.agencia.viajes.transaccional.rutas.dto;

import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaRutasResponse {
    private List<RutaDestino> contenido;
    private int totalPaginas;
    private long totalElementos;
    private int paginaActual;
    private boolean tieneSiguiente;
}

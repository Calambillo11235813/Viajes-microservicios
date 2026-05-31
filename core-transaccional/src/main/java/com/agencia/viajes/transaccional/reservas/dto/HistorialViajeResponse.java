package com.agencia.viajes.transaccional.reservas.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para representar una entrada en el historial de viajes de un usuario.
 */
@Data
@Builder
public class HistorialViajeResponse {
    private Integer idReserva;
    private Integer idViaje;
    private String ciudadOrigen;
    private String ciudadDestino;
    private String fechaCreacion;
    private String estadoReserva;
    private BigDecimal montoTotalPagado;
    private Integer cantidadPasajeros;
}

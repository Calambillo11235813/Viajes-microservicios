package com.agencia.viajes.transaccional.reservas.model;

import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Reserva transaccional creada por un usuario para un viaje programado.
 */
@Getter
@Setter
@Entity
@Table(name = "RESERVA")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_viaje", nullable = false)
    private ViajeProgramado viajeProgramado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_reserva", nullable = false, length = 30)
    private String estadoReserva;

    @Column(name = "monto_total_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotalPagado;

    @Column(name = "cantidad_pasajeros", nullable = false)
    private Integer cantidadPasajeros;
}

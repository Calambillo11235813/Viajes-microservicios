package com.agencia.viajes.transaccional.flotas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Unidad de transporte disponible para asignarse a viajes programados.
 */
@Getter
@Setter
@Entity
@Table(name = "FLOTA")
public class Flota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bus")
    private Integer id;

    @Column(name = "placa", nullable = false, unique = true, length = 20)
    private String placa;

    @Column(name = "capacidad_total_asientos", nullable = false)
    private Integer capacidadTotalAsientos;

    @Column(name = "tipo_bus", nullable = false, length = 50)
    private String tipoBus;
}

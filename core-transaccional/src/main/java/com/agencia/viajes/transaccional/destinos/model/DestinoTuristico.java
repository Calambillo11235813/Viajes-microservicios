package com.agencia.viajes.transaccional.destinos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un destino turístico mapeado a un departamento.
 */
@Getter
@Setter
@Entity
@Table(name = "DESTINO_TURISTICO")
public class DestinoTuristico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_destino")
    private Integer id;

    @Column(name = "nombre_turistico", nullable = false, length = 100, unique = true)
    private String nombreTuristico;

    @Column(name = "departamento", nullable = false, length = 100)
    private String departamento;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}

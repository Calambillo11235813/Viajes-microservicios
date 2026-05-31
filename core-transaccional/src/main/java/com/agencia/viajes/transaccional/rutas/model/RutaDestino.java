package com.agencia.viajes.transaccional.rutas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Ruta comercial entre una ciudad de origen y una ciudad de destino.
 */
@Getter
@Setter
@Entity
@Table(name = "RUTA_DESTINO")
public class RutaDestino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer id;

    @Column(name = "ciudad_origen", nullable = false, length = 100)
    private String ciudadOrigen;

    @Column(name = "ciudad_destino", nullable = false, length = 100)
    private String ciudadDestino;

    @Column(name = "duracion_estimada_horas", nullable = false, precision = 5, scale = 2)
    private BigDecimal duracionEstimadaHoras;

    @Column(name = "categoria_turistica", length = 50)
    private String categoriaTuristica;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;
}

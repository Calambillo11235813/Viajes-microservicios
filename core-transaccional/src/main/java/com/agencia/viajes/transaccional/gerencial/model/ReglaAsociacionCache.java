package com.agencia.viajes.transaccional.gerencial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Caché local de las reglas de asociación.
 */
@Getter
@Setter
@Entity
@Table(name = "REGLA_ASOCIACION_CACHE")
public class ReglaAsociacionCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "antecedents", nullable = false, columnDefinition = "TEXT")
    private String antecedents;

    @Column(name = "consequents", nullable = false, columnDefinition = "TEXT")
    private String consequents;

    @Column(name = "soporte", nullable = false, precision = 8, scale = 6)
    private BigDecimal soporte;

    @Column(name = "confianza", nullable = false, precision = 8, scale = 6)
    private BigDecimal confianza;

    @Column(name = "lift", nullable = false, precision = 8, scale = 4)
    private BigDecimal lift;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;
}

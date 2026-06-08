package com.agencia.viajes.transaccional.gerencial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Snapshot diario de KPIs.
 */
@Getter
@Setter
@Entity
@Table(name = "DASHBOARD_KPI_SNAPSHOT")
public class DashboardKpiSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_snapshot", nullable = false, unique = true)
    private LocalDate fechaSnapshot;

    @Column(name = "total_usuarios", nullable = false)
    private Integer totalUsuarios;

    @Column(name = "total_usuarios_segmentados", nullable = false)
    private Integer totalUsuariosSegmentados;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribucion_clusters", nullable = false, columnDefinition = "jsonb")
    private String distribucionClusters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingreso_por_cluster", nullable = false, columnDefinition = "jsonb")
    private String ingresoPorCluster;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conversion_por_cluster", nullable = false, columnDefinition = "jsonb")
    private String conversionPorCluster;

    @Column(name = "total_reglas_asociacion", nullable = false)
    private Integer totalReglasAsociacion;

    @Column(name = "reglas_alto_lift_count", nullable = false)
    private Integer reglasAltoLiftCount;

    @Column(name = "support_promedio_top20", precision = 8, scale = 6)
    private BigDecimal supportPromedioTop20;

    @Column(name = "indice_cross_selling", nullable = false)
    private Integer indiceCrossSelling;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}

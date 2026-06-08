package com.agencia.viajes.transaccional.gerencial.model;

import com.agencia.viajes.transaccional.usuarios.model.Usuario;
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
 * Historial de asignación de clústeres para un usuario.
 */
@Getter
@Setter
@Entity
@Table(name = "USUARIO_CLUSTER_HISTORICO")
public class UsuarioClusterHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "cluster_id", nullable = false)
    private Integer clusterId;

    @Column(name = "total_gastado", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGastado;

    @Column(name = "num_reservas", nullable = false)
    private Integer numReservas;

    @Column(name = "rutas_distintas", nullable = false)
    private Integer rutasDistintas;

    @Column(name = "promedio_pasajeros", nullable = false, precision = 5, scale = 2)
    private BigDecimal promedioPasajeros;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
}

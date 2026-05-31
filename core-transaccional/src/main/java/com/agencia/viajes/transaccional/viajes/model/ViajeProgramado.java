package com.agencia.viajes.transaccional.viajes.model;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Viaje concreto ofertado para una ruta, bus y horario determinado.
 */
@Getter
@Setter
@Entity
@Table(name = "VIAJE_PROGRAMADO")
public class ViajeProgramado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_viaje")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ruta", nullable = false)
    private RutaDestino rutaDestino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bus", nullable = false)
    private Flota flota;

    @Column(name = "fecha_hora_salida", nullable = false)
    private LocalDateTime fechaHoraSalida;

    @Column(name = "fecha_hora_llegada", nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Column(name = "estado_viaje", nullable = false, length = 30)
    private String estadoViaje;
}

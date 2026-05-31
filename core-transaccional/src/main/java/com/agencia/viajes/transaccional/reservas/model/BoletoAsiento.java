package com.agencia.viajes.transaccional.reservas.model;

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
 * Asiento reservado para un pasajero dentro de una reserva.
 */
@Getter
@Setter
@Entity
@Table(name = "BOLETO_ASIENTO")
public class BoletoAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @Column(name = "numero_asiento", nullable = false, length = 10)
    private String numeroAsiento;

    @Column(name = "nombre_pasajero", nullable = false, length = 100)
    private String nombrePasajero;

    @Column(name = "hash_blockchain", unique = true)
    private String hashBlockchain;

    @Column(name = "codigo_qr", unique = true)
    private String codigoQr;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "estado_boleto", nullable = false, length = 30)
    private String estadoBoleto;

    @Column(name = "tipo_pasajero", nullable = false, length = 30)
    private String tipoPasajero;
}

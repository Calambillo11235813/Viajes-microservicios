package com.agencia.viajes.transaccional.pagos.model;

import com.agencia.viajes.transaccional.reservas.model.Reserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Pago asociado a una reserva confirmada por acreditación.
 */
@Getter
@Setter
@Entity
@Table(name = "PAGO")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "monto_transaccion", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTransaccion;

    @Column(name = "metodo_pago_usado", nullable = false, length = 50)
    private String metodoPagoUsado;

    @Column(name = "cupon_descuento_aplicado", length = 50)
    private String cuponDescuentoAplicado;

    @Column(name = "estado_pago", nullable = false, length = 30)
    private String estadoPago;
}

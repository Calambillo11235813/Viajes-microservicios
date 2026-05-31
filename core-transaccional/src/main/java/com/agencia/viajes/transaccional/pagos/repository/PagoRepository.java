package com.agencia.viajes.transaccional.pagos.repository;

import com.agencia.viajes.transaccional.pagos.model.Pago;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de pagos.
 */
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /**
     * Verifica si una reserva ya tiene un pago registrado.
     *
     * @param idReserva identificador de la reserva.
     * @return true si existe pago para la reserva.
     */
    boolean existsByReservaId(Integer idReserva);

    /**
     * Recupera el pago asociado a una reserva.
     *
     * @param idReserva identificador de la reserva.
     * @return pago encontrado, si existe.
     */
    Optional<Pago> findByReservaId(Integer idReserva);

    /**
     * Consulta nativa para obtener las ventas agrupadas por fecha (solo pagos CONFIRMADO).
     *
     * @param inicio fecha y hora de inicio del rango.
     * @param fin fecha y hora de fin del rango.
     * @return lista de filas con [fecha, suma_monto, cantidad_pagos].
     */
    @Query(value = "SELECT CAST(fecha_pago AS date) AS fecha, SUM(monto_transaccion) AS monto, COUNT(*) AS cantidad " +
                   "FROM PAGO " +
                   "WHERE fecha_pago >= :inicio AND fecha_pago <= :fin AND estado_pago = 'CONFIRMADO' " +
                   "GROUP BY CAST(fecha_pago AS date) " +
                   "ORDER BY fecha ASC", nativeQuery = true)
    List<Object[]> findVentasAgrupadasPorFecha(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}


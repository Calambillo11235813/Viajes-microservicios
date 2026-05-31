package com.agencia.viajes.transaccional.reservas.repository;

import com.agencia.viajes.transaccional.reservas.model.BoletoAsiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de boletos y ocupación de asientos.
 */
public interface BoletoAsientoRepository extends JpaRepository<BoletoAsiento, Integer> {

    /**
     * Obtiene los asientos ocupados para un viaje por reservas activas.
     *
     * @param idViaje identificador del viaje programado.
     * @return números de asiento reservados o emitidos.
     */
    @Query("""
            SELECT boleto.numeroAsiento
            FROM BoletoAsiento boleto
            JOIN boleto.reserva reserva
            WHERE reserva.viajeProgramado.id = :idViaje
              AND reserva.estadoReserva IN ('PENDIENTE', 'CONFIRMADA')
              AND boleto.estadoBoleto <> 'ANULADO'
            """)
    List<String> buscarNumerosOcupadosPorViaje(@Param("idViaje") Integer idViaje);

    /**
     * Verifica si un asiento ya está ocupado por una reserva activa.
     *
     * @param idViaje identificador del viaje programado.
     * @param numeroAsiento asiento solicitado.
     * @return true si el asiento no puede reservarse.
     */
    @Query("""
            SELECT COUNT(boleto) > 0
            FROM BoletoAsiento boleto
            JOIN boleto.reserva reserva
            WHERE reserva.viajeProgramado.id = :idViaje
              AND UPPER(boleto.numeroAsiento) = UPPER(:numeroAsiento)
              AND reserva.estadoReserva IN ('PENDIENTE', 'CONFIRMADA')
              AND boleto.estadoBoleto <> 'ANULADO'
            """)
    boolean existeAsientoOcupado(
            @Param("idViaje") Integer idViaje,
            @Param("numeroAsiento") String numeroAsiento);

    /**
     * Obtiene los boletos asociados a una reserva.
     *
     * @param idReserva identificador de la reserva.
     * @return boletos de la reserva.
     */
    List<BoletoAsiento> findByReservaId(Integer idReserva);
}

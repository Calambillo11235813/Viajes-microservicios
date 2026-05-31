package com.agencia.viajes.transaccional.reservas.repository;

import com.agencia.viajes.transaccional.reservas.model.Reserva;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de reservas.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    /**
     * Recupera una reserva con bloqueo para confirmar pagos sin doble cobro.
     *
     * @param idReserva identificador de la reserva.
     * @return reserva bloqueada con usuario, viaje, ruta y flota cargados.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.usuario usuario
            JOIN FETCH reserva.viajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE reserva.id = :idReserva
            """)
    Optional<Reserva> buscarPorIdConBloqueoParaPago(@Param("idReserva") Integer idReserva);

    /**
     * Recupera una reserva con bloqueo pesimista para cancelación segura.
     *
     * @param idReserva identificador de la reserva.
     * @return reserva bloqueada con usuario y viaje cargados.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT reserva
            FROM Reserva reserva
            JOIN FETCH reserva.usuario usuario
            JOIN FETCH reserva.viajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            WHERE reserva.id = :idReserva
            """)
    Optional<Reserva> buscarPorIdConBloqueoParaCancelacion(@Param("idReserva") Integer idReserva);

    /**
     * Recupera el historial de viajes de un usuario ordenado por fecha de creación descendente.
     *
     * @param idUsuario identificador del usuario
     * @return Lista de reservas.
     */
    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.viajeProgramado v
            JOIN FETCH v.rutaDestino ru
            WHERE r.usuario.id = :idUsuario
            ORDER BY r.fechaCreacion DESC
            """)
    List<Reserva> buscarHistorialPorUsuario(@Param("idUsuario") Integer idUsuario);
}

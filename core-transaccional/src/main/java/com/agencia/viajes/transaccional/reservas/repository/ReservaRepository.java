package com.agencia.viajes.transaccional.reservas.repository;

import com.agencia.viajes.transaccional.reservas.model.Reserva;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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

    /**
     * Suma el gasto total de un usuario considerando solo reservas no canceladas.
     * Las reservas canceladas tienen su monto en cero, por lo que se excluyen
     * explícitamente para no distorsionar el cálculo del perfil.
     *
     * @param idUsuario identificador del usuario.
     * @return gasto total acumulado; {@code 0} si el usuario no tiene reservas válidas.
     */
    @Query("""
            SELECT COALESCE(SUM(r.montoTotalPagado), 0)
            FROM Reserva r
            WHERE r.usuario.id = :idUsuario
              AND r.estadoReserva <> 'CANCELADA'
            """)
    BigDecimal sumarGastoTotalUsuario(@Param("idUsuario") Integer idUsuario);

    /**
     * Obtiene el gasto total agregado por cada usuario (excluyendo reservas
     * canceladas). Se usa para calcular los percentiles globales que definen los
     * umbrales de perfil (Económico, Estándar, Premium).
     *
     * @return lista de gastos totales por usuario, uno por cada usuario con reservas.
     */
    @Query("""
            SELECT SUM(r.montoTotalPagado)
            FROM Reserva r
            WHERE r.estadoReserva <> 'CANCELADA'
            GROUP BY r.usuario.id
            """)
    List<BigDecimal> obtenerGastosTotalesPorUsuario();

    /**
     * Devuelve las categorías turísticas reservadas por un usuario ordenadas de
     * mayor a menor frecuencia. El primer elemento corresponde a la categoría
     * preferida. Se excluyen reservas canceladas y categorías nulas.
     *
     * @param idUsuario identificador del usuario.
     * @param pageable paginación; usar {@code PageRequest.of(0, 1)} para la más frecuente.
     * @return lista de categorías ordenadas por frecuencia descendente.
     */
    @Query("""
            SELECT rd.categoriaTuristica
            FROM Reserva r
            JOIN r.viajeProgramado v
            JOIN v.rutaDestino rd
            WHERE r.usuario.id = :idUsuario
              AND r.estadoReserva <> 'CANCELADA'
              AND rd.categoriaTuristica IS NOT NULL
            GROUP BY rd.categoriaTuristica
            ORDER BY COUNT(r) DESC
            """)
    List<String> obtenerCategoriasPorFrecuencia(
            @Param("idUsuario") Integer idUsuario,
            Pageable pageable);
}

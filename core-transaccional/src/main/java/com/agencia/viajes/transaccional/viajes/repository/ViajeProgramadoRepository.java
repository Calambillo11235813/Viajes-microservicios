package com.agencia.viajes.transaccional.viajes.repository;

import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Consultas de persistencia para viajes programados.
 */
public interface ViajeProgramadoRepository extends JpaRepository<ViajeProgramado, Integer> {

    /**
     * Busca viajes programados disponibles para una ruta y fecha calendario.
     */
    @Query("""
            SELECT viaje
            FROM ViajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE LOWER(ruta.ciudadOrigen) = LOWER(:origen)
              AND LOWER(ruta.ciudadDestino) = LOWER(:destino)
              AND viaje.fechaHoraSalida >= :inicioDia
              AND viaje.fechaHoraSalida < :finDia
              AND viaje.estadoViaje = 'PROGRAMADO'
            ORDER BY viaje.fechaHoraSalida ASC
            """)
    List<ViajeProgramado> buscarDisponiblesPorRutaYFecha(
            @Param("origen") String origen,
            @Param("destino") String destino,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia);

    /**
     * Busca viajes disponibles con paginación.
     */
    @Query("""
            SELECT viaje
            FROM ViajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE LOWER(ruta.ciudadOrigen) = LOWER(:origen)
              AND LOWER(ruta.ciudadDestino) = LOWER(:destino)
              AND viaje.fechaHoraSalida >= :inicioDia
              AND viaje.fechaHoraSalida < :finDia
              AND viaje.estadoViaje = 'PROGRAMADO'
            """)
    Page<ViajeProgramado> buscarDisponiblesPorRutaYFechaPaginado(
            @Param("origen") String origen,
            @Param("destino") String destino,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia,
            Pageable pageable);

    /**
     * Lista viajes de una ruta específica con paginación.
     */
    @Query("""
            SELECT v FROM ViajeProgramado v
            JOIN FETCH v.rutaDestino r
            JOIN FETCH v.flota f
            WHERE r.id = :idRuta
            """)
    Page<ViajeProgramado> buscarPorRutaPaginado(@Param("idRuta") Integer idRuta, Pageable pageable);

    /**
     * Recupera un viaje y bloquea su fila para serializar reservas concurrentes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT viaje
            FROM ViajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE viaje.id = :idViaje
            """)
    Optional<ViajeProgramado> buscarPorIdConBloqueo(@Param("idViaje") Integer idViaje);

    /**
     * Verifica si existe algún viaje programado para un bus que se solape con el rango de fechas.
     */
    @Query("""
            SELECT COUNT(v) FROM ViajeProgramado v
            WHERE v.flota.idBus = :idBus
              AND v.estadoViaje != 'CANCELADO'
              AND v.fechaHoraSalida < :fin
              AND v.fechaHoraLlegada > :inicio
            """)
    long countSolapamientos(
            @Param("idBus") Integer idBus,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT COUNT(v) FROM ViajeProgramado v
            WHERE v.flota.idBus = :idBus
              AND v.estadoViaje != 'CANCELADO'
              AND v.fechaHoraSalida < :fin
              AND v.fechaHoraLlegada > :inicio
              AND v.id <> :idViajeExcluido
            """)
    long countSolapamientosExcluyendo(
            @Param("idBus") Integer idBus,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("idViajeExcluido") Integer idViajeExcluido);

    @Query("""
            SELECT v FROM ViajeProgramado v
            JOIN FETCH v.rutaDestino r
            WHERE v.flota.idBus = :idBus
              AND v.estadoViaje != 'CANCELADO'
              AND v.fechaHoraSalida < :fin
              AND v.fechaHoraLlegada > :inicio
              AND v.id <> :idViajeExcluido
            ORDER BY v.fechaHoraSalida ASC
            """)
    List<ViajeProgramado> buscarSolapamientosExcluyendo(
            @Param("idBus") Integer idBus,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("idViajeExcluido") Integer idViajeExcluido);

    @Query("""
            SELECT v FROM ViajeProgramado v
            JOIN FETCH v.rutaDestino r
            WHERE v.flota.idBus = :idBus
              AND v.estadoViaje != 'CANCELADO'
              AND v.fechaHoraSalida < :fin
              AND v.fechaHoraLlegada > :inicio
            ORDER BY v.fechaHoraSalida ASC
            """)
    List<ViajeProgramado> buscarSolapamientos(
            @Param("idBus") Integer idBus,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Busca viajes programados futuros hacia un destino específico (desde cualquier origen).
     *
     * @param destino ciudad de destino solicitada.
     * @param momentoActual fecha y hora a partir de la cual buscar.
     * @return viajes programados futuros hacia ese destino.
     */
    @Query("""
            SELECT viaje
            FROM ViajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE LOWER(ruta.ciudadDestino) = LOWER(:destino)
              AND viaje.fechaHoraSalida > :momentoActual
              AND viaje.estadoViaje = 'PROGRAMADO'
            ORDER BY viaje.fechaHoraSalida ASC
            """)
    List<ViajeProgramado> buscarDisponiblesHaciaDestinoFuturos(
            @Param("destino") String destino,
            @Param("momentoActual") LocalDateTime momentoActual);

    /**
     * Busca viajes programados futuros hacia un destino específico con paginación y filtros opcionales.
     *
     * @param destino ciudad de destino solicitada.
     * @param origen filtro opcional por ciudad de origen (cadena vacía para ignorar).
     * @param inicioDia filtro por fecha de inicio.
     * @param finDia filtro por fecha de fin.
     * @param pageable paginación.
     * @return página de viajes programados futuros.
     */
    @Query("""
            SELECT viaje
            FROM ViajeProgramado viaje
            JOIN FETCH viaje.rutaDestino ruta
            JOIN FETCH viaje.flota flota
            WHERE LOWER(ruta.ciudadDestino) = LOWER(:destino)
              AND viaje.estadoViaje = 'PROGRAMADO'
              AND (:origen = '' OR LOWER(ruta.ciudadOrigen) = LOWER(:origen))
              AND viaje.fechaHoraSalida >= :inicioDia
              AND viaje.fechaHoraSalida < :finDia
            ORDER BY viaje.fechaHoraSalida ASC
            """)
    org.springframework.data.domain.Page<ViajeProgramado> buscarDisponiblesHaciaDestinoPaginado(
            @Param("destino") String destino,
            @Param("origen") String origen,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia,
            org.springframework.data.domain.Pageable pageable);
}

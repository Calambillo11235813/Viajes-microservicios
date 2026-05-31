package com.agencia.viajes.transaccional.viajes.repository;

import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
     *
     * @param origen ciudad de origen solicitada.
     * @param destino ciudad de destino solicitada.
     * @param inicioDia límite inferior inclusivo de la fecha de salida.
     * @param finDia límite superior exclusivo de la fecha de salida.
     * @return viajes programados que coinciden con los criterios.
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
     * Recupera un viaje y bloquea su fila para serializar reservas concurrentes.
     *
     * @param idViaje identificador del viaje programado.
     * @return viaje bloqueado con ruta y flota cargadas.
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
     * Excluye un ID de viaje específico para soportar actualizaciones.
     */
    @Query("""
            SELECT COUNT(v) FROM ViajeProgramado v
            WHERE v.flota.id = :idBus
              AND v.estadoViaje != 'CANCELADO'
              AND v.fechaHoraSalida < :fin
              AND v.fechaHoraLlegada > :inicio
              AND (:idViajeExcluido IS NULL OR v.id != :idViajeExcluido)
            """)
    long countSolapamientos(
            @Param("idBus") Integer idBus,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("idViajeExcluido") Integer idViajeExcluido);
}

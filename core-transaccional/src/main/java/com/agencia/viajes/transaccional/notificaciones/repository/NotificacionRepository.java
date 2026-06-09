package com.agencia.viajes.transaccional.notificaciones.repository;

import com.agencia.viajes.transaccional.notificaciones.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de notificaciones.
 */
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @Query("""
            SELECT n FROM Notificacion n
            WHERE n.usuario.id = :idUsuario
            ORDER BY n.fechaCreacion DESC
            """)
    Page<Notificacion> buscarPorUsuario(@Param("idUsuario") Integer idUsuario, Pageable pageable);

    @Query("""
            SELECT n FROM Notificacion n
            WHERE n.usuario.id = :idUsuario AND n.leido = true
            ORDER BY n.fechaCreacion DESC
            """)
    Page<Notificacion> buscarLeidasPorUsuario(@Param("idUsuario") Integer idUsuario, Pageable pageable);

    @Query("""
            SELECT n FROM Notificacion n
            WHERE n.usuario.id = :idUsuario AND n.leido = false
            ORDER BY n.fechaCreacion DESC
            """)
    Page<Notificacion> buscarNoLeidasPorUsuario(@Param("idUsuario") Integer idUsuario, Pageable pageable);

    long countByUsuarioIdAndLeidoFalse(Integer idUsuario);

    @Modifying
    @Query("""
            UPDATE Notificacion n SET n.leido = true
            WHERE n.usuario.id = :idUsuario AND n.leido = false
            """)
    int marcarTodasLeidasPorUsuario(@Param("idUsuario") Integer idUsuario);
}

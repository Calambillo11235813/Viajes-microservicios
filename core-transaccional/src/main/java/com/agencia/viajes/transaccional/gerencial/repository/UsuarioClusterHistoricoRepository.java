package com.agencia.viajes.transaccional.gerencial.repository;

import com.agencia.viajes.transaccional.gerencial.model.UsuarioClusterHistorico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioClusterHistoricoRepository extends JpaRepository<UsuarioClusterHistorico, Integer> {

    @Query(value = """
            SELECT * FROM USUARIO_CLUSTER_HISTORICO
            WHERE id_usuario = :idUsuario
            ORDER BY fecha_asignacion DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<UsuarioClusterHistorico> findLatestByUsuarioId(@Param("idUsuario") Integer idUsuario);
}

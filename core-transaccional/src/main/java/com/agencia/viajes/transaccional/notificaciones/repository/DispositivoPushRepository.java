package com.agencia.viajes.transaccional.notificaciones.repository;

import com.agencia.viajes.transaccional.notificaciones.model.DispositivoPush;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de tokens push.
 */
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush, Integer> {

    Optional<DispositivoPush> findByToken(String token);

    List<DispositivoPush> findByUsuarioIdInAndActivoTrue(List<Integer> idsUsuario);
}

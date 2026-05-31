package com.agencia.viajes.transaccional.usuarios.repository;

import com.agencia.viajes.transaccional.usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de los roles del sistema.
 */
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}

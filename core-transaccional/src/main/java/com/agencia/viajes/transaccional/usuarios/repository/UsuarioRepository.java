package com.agencia.viajes.transaccional.usuarios.repository;

import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de usuarios registrados en el sistema.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo a buscar.
     * @return usuario encontrado, si existe.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su número de CI o pasaporte.
     *
     * @param ciPasaporte documento de identidad a buscar.
     * @return usuario encontrado, si existe.
     */
    Optional<Usuario> findByCiPasaporte(String ciPasaporte);
}

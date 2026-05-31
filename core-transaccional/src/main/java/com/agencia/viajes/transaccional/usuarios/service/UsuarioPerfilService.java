package com.agencia.viajes.transaccional.usuarios.service;

import com.agencia.viajes.transaccional.usuarios.dto.UsuarioPerfilResponse;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión del perfil de usuario (CU-05).
 * Permite al cliente consultar y modificar sus datos personales de contacto.
 */
@Service
@RequiredArgsConstructor
public class UsuarioPerfilService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Recupera el perfil público de un usuario por su identificador.
     *
     * @param idUsuario identificador del usuario.
     * @return datos del perfil sin información sensible.
     * @throws IllegalArgumentException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioPerfilResponse obtenerPerfil(Integer idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El identificador de usuario es obligatorio.");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        return mapearRespuesta(usuario);
    }

    /**
     * Actualiza los datos personales de contacto de un usuario.
     * Solo permite modificar nombre, email, teléfono y CI/pasaporte.
     * Valida unicidad de email y CI antes de persistir.
     *
     * @param idUsuario identificador del usuario a actualizar.
     * @param nombreCompleto nuevo nombre completo (opcional, se ignora si es nulo).
     * @param email nuevo correo electrónico (opcional, se ignora si es nulo).
     * @param telefono nuevo número de teléfono (opcional, se ignora si es nulo).
     * @param ciPasaporte nuevo CI o pasaporte (opcional, se ignora si es nulo).
     * @return perfil actualizado del usuario.
     * @throws IllegalArgumentException si el usuario no existe o hay conflictos de unicidad.
     */
    @Transactional
    public UsuarioPerfilResponse actualizarPerfil(
            Integer idUsuario,
            String nombreCompleto,
            String email,
            String telefono,
            String ciPasaporte) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El identificador de usuario es obligatorio.");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        actualizarNombre(usuario, nombreCompleto);
        actualizarEmail(usuario, email);
        actualizarTelefono(usuario, telefono);
        actualizarCiPasaporte(usuario, ciPasaporte);

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapearRespuesta(actualizado);
    }

    private void actualizarNombre(Usuario usuario, String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return;
        }
        usuario.setNombreCompleto(nombreCompleto.trim());
    }

    private void actualizarEmail(Usuario usuario, String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (emailNormalizado.equals(usuario.getEmail())) {
            return;
        }
        Optional<Usuario> existente = usuarioRepository.findByEmail(emailNormalizado);
        if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado por otro usuario.");
        }
        usuario.setEmail(emailNormalizado);
    }

    private void actualizarTelefono(Usuario usuario, String telefono) {
        if (telefono == null) {
            return;
        }
        usuario.setTelefono(telefono.isBlank() ? null : telefono.trim());
    }

    private void actualizarCiPasaporte(Usuario usuario, String ciPasaporte) {
        if (ciPasaporte == null || ciPasaporte.isBlank()) {
            return;
        }
        String ciNormalizado = ciPasaporte.trim();
        if (ciNormalizado.equals(usuario.getCiPasaporte())) {
            return;
        }
        Optional<Usuario> existente = usuarioRepository.findByCiPasaporte(ciNormalizado);
        if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("El CI/Pasaporte ya está registrado por otro usuario.");
        }
        usuario.setCiPasaporte(ciNormalizado);
    }

    private UsuarioPerfilResponse mapearRespuesta(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getCiPasaporte(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getIdRol());
    }
}

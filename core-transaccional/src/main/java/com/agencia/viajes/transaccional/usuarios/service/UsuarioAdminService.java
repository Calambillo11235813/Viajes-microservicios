package com.agencia.viajes.transaccional.usuarios.service;

import com.agencia.viajes.transaccional.usuarios.dto.UsuarioPerfilResponse;
import com.agencia.viajes.transaccional.usuarios.model.Rol;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.RolRepository;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio administrativo para la gestión de usuarios (CU-08).
 * Permite el CRUD completo de usuarios y asignación de roles.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<UsuarioPerfilResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    @Transactional
    public UsuarioPerfilResponse crearUsuario(String ciPasaporte, String nombreCompleto, String email, String passwordHash, String telefono, Integer idRol) {
        validarUnicidadCiPasaporte(ciPasaporte, null);
        validarUnicidadEmail(email, null);
        validarRol(idRol);

        Usuario nuevo = new Usuario();
        nuevo.setCiPasaporte(ciPasaporte.trim());
        nuevo.setNombreCompleto(nombreCompleto.trim());
        nuevo.setEmail(email.trim().toLowerCase());
        nuevo.setPasswordHash(passwordHash);
        nuevo.setTelefono(telefono != null && !telefono.isBlank() ? telefono.trim() : null);
        nuevo.setIdRol(idRol);

        return mapearRespuesta(usuarioRepository.save(nuevo));
    }

    @Transactional
    public UsuarioPerfilResponse actualizarUsuario(Integer idUsuario, String nombreCompleto, String email, String telefono, Integer idRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));

        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            usuario.setNombreCompleto(nombreCompleto.trim());
        }
        if (email != null && !email.isBlank()) {
            validarUnicidadEmail(email, idUsuario);
            usuario.setEmail(email.trim().toLowerCase());
        }
        if (telefono != null) {
            usuario.setTelefono(telefono.isBlank() ? null : telefono.trim());
        }
        if (idRol != null) {
            validarRol(idRol);
            usuario.setIdRol(idRol);
        }

        return mapearRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public boolean eliminarUsuario(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario);
        }
        // En un sistema real se validarían dependencias (reservas, pagos).
        // Por simplicidad, se permite borrar o podría marcarse como inactivo.
        usuarioRepository.deleteById(idUsuario);
        return true;
    }

    private void validarUnicidadCiPasaporte(String ciPasaporte, Integer idExcluido) {
        Optional<Usuario> existente = usuarioRepository.findByCiPasaporte(ciPasaporte.trim());
        if (existente.isPresent() && !existente.get().getId().equals(idExcluido)) {
            throw new IllegalArgumentException("Ya existe un usuario con el CI/Pasaporte: " + ciPasaporte);
        }
    }

    private void validarUnicidadEmail(String email, Integer idExcluido) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (existente.isPresent() && !existente.get().getId().equals(idExcluido)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + email);
        }
    }

    private void validarRol(Integer idRol) {
        if (!rolRepository.existsById(idRol)) {
            throw new IllegalArgumentException("El rol especificado no existe: " + idRol);
        }
    }

    private UsuarioPerfilResponse mapearRespuesta(Usuario usuario) {
        return new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getCiPasaporte(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getIdRol()
        );
    }
}

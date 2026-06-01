package com.agencia.viajes.transaccional.usuarios.service;

import com.agencia.viajes.transaccional.usuarios.dto.AuthResponse;
import com.agencia.viajes.transaccional.usuarios.dto.UsuarioPerfilResponse;
import com.agencia.viajes.transaccional.usuarios.model.Rol;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.RolRepository;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio para autenticación y autorización (Login).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    /**
     * Valida las credenciales e inicia sesión.
     * En un entorno real esto utilizaría Spring Security para validar el hash y generar un JWT real.
     *
     * @param email correo del usuario
     * @param password contraseña ingresada (en este paso simulamos comparación plana)
     * @return AuthResponse con un token (simulado) y el perfil.
     */
    public AuthResponse autenticar(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        // TODO: Reemplazar por contraseña encriptada usando BCryptPasswordEncoder
        if (!usuario.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        // Validar el rol del usuario
        Rol rol = rolRepository.findById(usuario.getIdRol())
                .orElseThrow(() -> new IllegalStateException("Rol no válido o no encontrado"));

        String nombreRol = rol.getNombre().toUpperCase();
        if (!nombreRol.equals("CLIENTE") && !nombreRol.equals("PASAJERO") && !nombreRol.equals("ADMINISTRADOR") && !nombreRol.equals("GERENTE")) {
            throw new SecurityException("Acceso denegado: El usuario no tiene un rol permitido (Cliente, Administrador o Gerente).");
        }

        // TODO: Reemplazar por un token JWT real
        String tokenMock = UUID.randomUUID().toString();

        UsuarioPerfilResponse perfil = new UsuarioPerfilResponse(
                usuario.getId(),
                usuario.getCiPasaporte(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getIdRol()
        );

        return new AuthResponse(tokenMock, perfil);
    }
}
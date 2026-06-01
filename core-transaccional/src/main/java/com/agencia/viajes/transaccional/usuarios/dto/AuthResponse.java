package com.agencia.viajes.transaccional.usuarios.dto;

/**
 * Respuesta de autenticación al iniciar sesión.
 */
public record AuthResponse(
        String token,
        UsuarioPerfilResponse usuario) {
}
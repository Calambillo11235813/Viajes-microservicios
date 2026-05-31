package com.agencia.viajes.transaccional.usuarios.dto;

/**
 * Proyección del perfil público de un usuario registrado.
 * Excluye datos sensibles como el hash de contraseña.
 */
public record UsuarioPerfilResponse(
        Integer idUsuario,
        String ciPasaporte,
        String nombreCompleto,
        String email,
        String telefono,
        Integer idRol) {
}

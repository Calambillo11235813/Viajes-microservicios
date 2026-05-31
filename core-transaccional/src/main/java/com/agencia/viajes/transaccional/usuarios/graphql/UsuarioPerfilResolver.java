package com.agencia.viajes.transaccional.usuarios.graphql;

import com.agencia.viajes.transaccional.usuarios.dto.UsuarioPerfilResponse;
import com.agencia.viajes.transaccional.usuarios.service.UsuarioPerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Operaciones GraphQL para la gestión del perfil de usuario (CU-05).
 */
@Controller
@RequiredArgsConstructor
public class UsuarioPerfilResolver {

    private final UsuarioPerfilService usuarioPerfilService;

    /**
     * Consulta el perfil público de un usuario registrado.
     *
     * @param idUsuario identificador del usuario.
     * @return datos del perfil sin información sensible.
     */
    @QueryMapping
    public UsuarioPerfilResponse obtenerPerfilUsuario(@Argument Integer idUsuario) {
        return usuarioPerfilService.obtenerPerfil(idUsuario);
    }

    /**
     * Actualiza los datos personales de contacto de un usuario.
     *
     * @param idUsuario identificador del usuario a actualizar.
     * @param nombreCompleto nuevo nombre completo (opcional).
     * @param email nuevo correo electrónico (opcional).
     * @param telefono nuevo teléfono (opcional).
     * @param ciPasaporte nuevo CI o pasaporte (opcional).
     * @return perfil actualizado.
     */
    @MutationMapping
    public UsuarioPerfilResponse actualizarPerfilUsuario(
            @Argument Integer idUsuario,
            @Argument String nombreCompleto,
            @Argument String email,
            @Argument String telefono,
            @Argument String ciPasaporte) {
        return usuarioPerfilService.actualizarPerfil(
                idUsuario,
                nombreCompleto,
                email,
                telefono,
                ciPasaporte);
    }
}

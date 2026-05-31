package com.agencia.viajes.transaccional.usuarios.resolver;

import com.agencia.viajes.transaccional.usuarios.dto.UsuarioPerfilResponse;
import com.agencia.viajes.transaccional.usuarios.model.Rol;
import com.agencia.viajes.transaccional.usuarios.service.UsuarioAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controlador de GraphQL para las operaciones administrativas de usuarios (CU-08).
 */
@Controller
@RequiredArgsConstructor
public class UsuarioAdminResolver {

    private final UsuarioAdminService usuarioAdminService;

    @QueryMapping
    public List<UsuarioPerfilResponse> listarUsuarios() {
        return usuarioAdminService.listarUsuarios();
    }

    @QueryMapping
    public List<Rol> listarRoles() {
        return usuarioAdminService.listarRoles();
    }

    @MutationMapping
    public UsuarioPerfilResponse crearUsuario(
            @Argument String ciPasaporte,
            @Argument String nombreCompleto,
            @Argument String email,
            @Argument String passwordHash,
            @Argument String telefono,
            @Argument Integer idRol) {
        return usuarioAdminService.crearUsuario(ciPasaporte, nombreCompleto, email, passwordHash, telefono, idRol);
    }

    @MutationMapping
    public UsuarioPerfilResponse actualizarUsuario(
            @Argument Integer idUsuario,
            @Argument String nombreCompleto,
            @Argument String email,
            @Argument String telefono,
            @Argument Integer idRol) {
        return usuarioAdminService.actualizarUsuario(idUsuario, nombreCompleto, email, telefono, idRol);
    }

    @MutationMapping
    public Boolean eliminarUsuario(@Argument Integer idUsuario) {
        return usuarioAdminService.eliminarUsuario(idUsuario);
    }
}

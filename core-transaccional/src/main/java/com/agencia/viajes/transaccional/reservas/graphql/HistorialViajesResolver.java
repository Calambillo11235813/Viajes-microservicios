package com.agencia.viajes.transaccional.reservas.graphql;

import com.agencia.viajes.transaccional.reservas.dto.PaginaHistorialResponse;
import com.agencia.viajes.transaccional.reservas.service.HistorialViajesService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador de GraphQL para el historial de viajes (paginado).
 */
@Controller
@RequiredArgsConstructor
public class HistorialViajesResolver {

    private final HistorialViajesService historialViajesService;

    @QueryMapping
    public PaginaHistorialResponse consultarHistorialViajes(
            @Argument Integer idUsuario,
            @Argument Integer pagina,
            @Argument Integer tamanio,
            graphql.GraphQLContext context) {
        
        com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");
        if (!usuario.getIdUsuario().equals(idUsuario) && !"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("No tienes permiso para ver el historial de otro usuario");
        }

        int p = (pagina != null) ? pagina : 0;
        int t = (tamanio != null) ? tamanio : 10;
        return historialViajesService.obtenerHistorialViajesPaginado(idUsuario, p, t);
    }
}

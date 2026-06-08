package com.agencia.viajes.transaccional.flotas.graphql;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.flotas.service.FlotaService;
import lombok.RequiredArgsConstructor;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FlotaResolver {

    private final FlotaService flotaService;

    private void verificarLectura(GraphQLContext context) {
        UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");
        if (!"ADMINISTRADOR".equals(usuario.getRol()) && !"GERENTE".equals(usuario.getRol())) {
            throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Acceso denegado: Se requiere rol ADMINISTRADOR o GERENTE");
        }
    }

    private void verificarAdminEstricto(GraphQLContext context) {
        UsuarioAutenticado usuario = context.get("usuarioAutenticado");
        if (usuario == null) throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Token inválido o expirado");
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException("Acceso denegado: Se requiere rol ADMINISTRADOR");
        }
    }

    @QueryMapping
    public List<Flota> listarFlotas(GraphQLContext context) {
        verificarLectura(context);
        return flotaService.listarFlotas();
    }

    @MutationMapping
    public Flota crearFlota(@Argument String placa,
                            @Argument Integer capacidadTotalAsientos,
                            @Argument String tipoBus,
                            GraphQLContext context) {
        verificarAdminEstricto(context);
        return flotaService.crearFlota(placa, capacidadTotalAsientos, tipoBus);
    }

    @MutationMapping
    public Flota actualizarFlota(@Argument Integer idBus,
                                 @Argument String placa,
                                 @Argument Integer capacidadTotalAsientos,
                                 @Argument String tipoBus,
                                 GraphQLContext context) {
        verificarAdminEstricto(context);
        return flotaService.actualizarFlota(idBus, placa, capacidadTotalAsientos, tipoBus);
    }

    @MutationMapping
    public Boolean eliminarFlota(@Argument Integer idBus, GraphQLContext context) {
        verificarAdminEstricto(context);
        return flotaService.eliminarFlota(idBus);
    }
}

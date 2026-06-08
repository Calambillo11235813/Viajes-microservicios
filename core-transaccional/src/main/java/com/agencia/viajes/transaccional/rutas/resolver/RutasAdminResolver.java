package com.agencia.viajes.transaccional.rutas.resolver;

import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.rutas.service.RutasAdminService;
import com.agencia.viajes.transaccional.viajes.dto.PaginaViajesResponse;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador de GraphQL para las operaciones administrativas de rutas y viajes (CU-09).
 */
@Controller
@RequiredArgsConstructor
public class RutasAdminResolver {

    private final RutasAdminService rutasAdminService;

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
    public com.agencia.viajes.transaccional.rutas.dto.PaginaRutasResponse listarRutas(
            @Argument Integer pagina,
            @Argument Integer tamanio,
            GraphQLContext context) {
        verificarLectura(context);
        int p = (pagina != null) ? pagina : 0;
        int t = (tamanio != null) ? tamanio : 10;
        return rutasAdminService.listarRutas(p, t);
    }

    @QueryMapping
    public PaginaViajesResponse listarViajesPorRuta(
            @Argument Integer idRuta,
            @Argument Integer pagina,
            @Argument Integer tamanio,
            GraphQLContext context) {
        verificarLectura(context);
        int p = (pagina != null) ? pagina : 0;
        int t = (tamanio != null) ? tamanio : 10;
        return rutasAdminService.listarViajesPorRuta(idRuta, p, t);
    }

    @MutationMapping
    public RutaDestino crearRuta(
            @Argument String origen,
            @Argument String destino,
            @Argument Double duracion,
            @Argument Double precio,
            @Argument String categoria,
            GraphQLContext context) {
        verificarAdminEstricto(context);
        return rutasAdminService.crearRuta(origen, destino, BigDecimal.valueOf(duracion), BigDecimal.valueOf(precio), categoria);
    }

    @MutationMapping
    public RutaDestino actualizarRuta(
            @Argument Integer idRuta,
            @Argument String origen,
            @Argument String destino,
            @Argument Double duracion,
            @Argument Double precio,
            @Argument String categoria,
            GraphQLContext context) {
        verificarAdminEstricto(context);
        BigDecimal duracionDecimal = duracion != null ? BigDecimal.valueOf(duracion) : null;
        BigDecimal precioDecimal = precio != null ? BigDecimal.valueOf(precio) : null;
        return rutasAdminService.actualizarRuta(idRuta, origen, destino, duracionDecimal, precioDecimal, categoria);
    }

    @MutationMapping
    public Boolean eliminarRuta(@Argument Integer idRuta, GraphQLContext context) {
        verificarAdminEstricto(context);
        return rutasAdminService.eliminarRuta(idRuta);
    }

    @MutationMapping
    public ViajeDisponibleResponse programarViaje(
            @Argument Integer idRuta,
            @Argument Integer idBus,
            @Argument String fechaHoraSalida,
            GraphQLContext context) {
        verificarAdminEstricto(context);
        return rutasAdminService.programarViaje(idRuta, idBus, fechaHoraSalida);
    }

    @MutationMapping
    public ViajeDisponibleResponse actualizarViajeProgramado(
            @Argument Integer idViaje,
            @Argument Integer idBus,
            @Argument String fechaHoraSalida,
            GraphQLContext context) {
        verificarAdminEstricto(context);
        return rutasAdminService.actualizarViajeProgramado(idViaje, idBus, fechaHoraSalida);
    }

    @MutationMapping
    public Boolean cancelarViajeProgramado(@Argument Integer idViaje, GraphQLContext context) {
        verificarAdminEstricto(context);
        return rutasAdminService.cancelarViajeProgramado(idViaje);
    }
}

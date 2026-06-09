package com.agencia.viajes.transaccional.notificaciones.graphql;

import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionResponse;
import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionUsuarioInput;
import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionViajeInput;
import com.agencia.viajes.transaccional.notificaciones.dto.PaginaNotificacionesResponse;
import com.agencia.viajes.transaccional.notificaciones.dto.RegistrarDispositivoPushInput;
import com.agencia.viajes.transaccional.notificaciones.service.DispositivoPushService;
import com.agencia.viajes.transaccional.notificaciones.service.NotificacionService;
import com.agencia.viajes.transaccional.usuarios.security.AccessDeniedException;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador GraphQL para notificaciones (CU-13).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificacionResolver {

    private final NotificacionService notificacionService;
    private final DispositivoPushService dispositivoPushService;

    @QueryMapping
    public PaginaNotificacionesResponse obtenerNotificacionesUsuario(
            @Argument Integer idUsuario,
            @Argument String estado,
            @Argument Integer pagina,
            @Argument Integer tamanio,
            GraphQLContext context) {
        verificarAccesoUsuario(idUsuario, context);
        int p = pagina != null ? pagina : 0;
        int t = tamanio != null ? tamanio : 20;
        return notificacionService.obtenerNotificacionesUsuario(idUsuario, estado, p, t);
    }

    @QueryMapping
    public Integer contarNotificacionesNoLeidas(
            @Argument Integer idUsuario,
            GraphQLContext context) {
        verificarAccesoUsuario(idUsuario, context);
        return (int) notificacionService.contarNoLeidas(idUsuario);
    }

    @MutationMapping
    public List<NotificacionResponse> enviarNotificacionPorViaje(
            @Argument NotificacionViajeInput input,
            GraphQLContext context) {
        UsuarioAutenticado admin = obtenerUsuario(context);
        log.info(
                "[Notif] GraphQL enviarNotificacionPorViaje | admin={} idViaje={} tipo={}",
                admin != null ? admin.getIdUsuario() : null,
                input != null ? input.getIdViaje() : null,
                input != null ? input.getTipo() : null);
        verificarAdmin(context);
        List<NotificacionResponse> respuesta = notificacionService.enviarPorViaje(input);
        log.info(
                "[Notif] GraphQL enviarNotificacionPorViaje OK | enviadas={}",
                respuesta.size());
        return respuesta;
    }

    @MutationMapping
    public List<NotificacionResponse> enviarNotificacionPorUsuario(
            @Argument NotificacionUsuarioInput input,
            GraphQLContext context) {
        UsuarioAutenticado admin = obtenerUsuario(context);
        log.info(
                "[Notif] GraphQL enviarNotificacionPorUsuario | admin={} tipo={} usuarios={}",
                admin != null ? admin.getIdUsuario() : null,
                input != null ? input.getTipo() : null,
                input != null ? input.getIdsUsuario().size() : 0);
        verificarAdmin(context);
        List<NotificacionResponse> respuesta = notificacionService.enviarPorUsuarios(input);
        log.info(
                "[Notif] GraphQL enviarNotificacionPorUsuario OK | enviadas={}",
                respuesta.size());
        return respuesta;
    }

    @MutationMapping
    public NotificacionResponse marcarNotificacionLeida(
            @Argument String id,
            GraphQLContext context) {
        UsuarioAutenticado usuario = obtenerUsuario(context);
        NotificacionResponse notificacion = notificacionService.marcarLeida(Integer.valueOf(id));
        if (!usuario.getIdUsuario().equals(notificacion.getIdUsuario())
                && !"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new AccessDeniedException("No tienes permiso para marcar esta notificación");
        }
        return notificacion;
    }

    @MutationMapping
    public Boolean marcarTodasNotificacionesLeidas(
            @Argument Integer idUsuario,
            GraphQLContext context) {
        verificarAccesoUsuario(idUsuario, context);
        return notificacionService.marcarTodasLeidas(idUsuario);
    }

    @MutationMapping
    public Boolean registrarDispositivoPush(
            @Argument RegistrarDispositivoPushInput input,
            GraphQLContext context) {
        verificarAccesoUsuario(input.getIdUsuario(), context);
        return dispositivoPushService.registrarToken(input);
    }

    @MutationMapping
    public Boolean desactivarDispositivoPush(
            @Argument String token,
            GraphQLContext context) {
        if (obtenerUsuario(context) == null) {
            throw new AccessDeniedException("Token inválido o expirado");
        }
        return dispositivoPushService.desactivarToken(token);
    }

    private void verificarAccesoUsuario(Integer idUsuario, GraphQLContext context) {
        UsuarioAutenticado usuario = obtenerUsuario(context);
        if (usuario == null) {
            throw new AccessDeniedException("Token inválido o expirado");
        }
        if (!usuario.getIdUsuario().equals(idUsuario) && !"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new AccessDeniedException("No tienes permiso para acceder a las notificaciones de otro usuario");
        }
    }

    private void verificarAdmin(GraphQLContext context) {
        UsuarioAutenticado usuario = obtenerUsuario(context);
        if (usuario == null) {
            throw new AccessDeniedException("Token inválido o expirado");
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol())) {
            throw new AccessDeniedException("Acceso denegado: Se requiere rol ADMINISTRADOR");
        }
    }

    private UsuarioAutenticado obtenerUsuario(GraphQLContext context) {
        return context.get("usuarioAutenticado");
    }
}

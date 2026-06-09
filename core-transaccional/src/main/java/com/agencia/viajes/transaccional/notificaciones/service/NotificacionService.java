package com.agencia.viajes.transaccional.notificaciones.service;

import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionResponse;
import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionUsuarioInput;
import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionViajeInput;
import com.agencia.viajes.transaccional.notificaciones.dto.PaginaNotificacionesResponse;
import com.agencia.viajes.transaccional.notificaciones.model.DispositivoPush;
import com.agencia.viajes.transaccional.notificaciones.model.Notificacion;
import com.agencia.viajes.transaccional.notificaciones.push.PushMessage;
import com.agencia.viajes.transaccional.notificaciones.push.PushNotificationService;
import com.agencia.viajes.transaccional.notificaciones.repository.DispositivoPushRepository;
import com.agencia.viajes.transaccional.notificaciones.repository.NotificacionRepository;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Servicio principal de notificaciones persistidas y envío push.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "EMERGENCIA_RUTA",
            "DOCUMENTACION_FALTANTE",
            "CAMBIO_HORARIO",
            "CANCELACION",
            "RETRASO");

    private final NotificacionRepository notificacionRepository;
    private final DispositivoPushRepository dispositivoPushRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public List<NotificacionResponse> enviarPorViaje(NotificacionViajeInput input) {
        log.info(
                "[Notif] Solicitud por viaje | idViaje={} tipo={} titulo=\"{}\"",
                input != null ? input.getIdViaje() : null,
                input != null ? input.getTipo() : null,
                input != null ? input.getTitulo() : null);
        validarInputViaje(input);
        validarTipo(input.getTipo());

        if (!viajeProgramadoRepository.existsById(input.getIdViaje())) {
            log.warn("[Notif] Viaje no encontrado | idViaje={}", input.getIdViaje());
            throw new IllegalArgumentException("Viaje no encontrado: " + input.getIdViaje());
        }

        List<Integer> idsUsuarios = buscarDestinatariosPorViaje(input.getIdViaje(), input.getTipo());
        if (idsUsuarios.isEmpty()) {
            log.warn(
                    "[Notif] Sin pasajeros para notificar | idViaje={} tipo={}",
                    input.getIdViaje(),
                    input.getTipo());
            return List.of();
        }

        log.info(
                "[Notif] Pasajeros a notificar | idViaje={} tipo={} cantidad={} ids={}",
                input.getIdViaje(),
                input.getTipo(),
                idsUsuarios.size(),
                idsUsuarios);

        return crearYEnviarNotificaciones(
                idsUsuarios,
                input.getTipo(),
                input.getTitulo(),
                input.getMensaje(),
                input.getDatosExtraJson());
    }

    @Transactional
    public List<NotificacionResponse> enviarPorUsuarios(NotificacionUsuarioInput input) {
        log.info(
                "[Notif] Solicitud por usuarios | tipo={} titulo=\"{}\" ids={}",
                input != null ? input.getTipo() : null,
                input != null ? input.getTitulo() : null,
                input != null ? input.getIdsUsuario() : null);
        validarInputUsuarios(input);
        validarTipo(input.getTipo());

        List<Integer> idsUnicos = input.getIdsUsuario().stream().distinct().toList();
        for (Integer idUsuario : idsUnicos) {
            if (!usuarioRepository.existsById(idUsuario)) {
                throw new IllegalArgumentException("Usuario no encontrado: " + idUsuario);
            }
        }

        return crearYEnviarNotificaciones(
                idsUnicos,
                input.getTipo(),
                input.getTitulo(),
                input.getMensaje(),
                input.getDatosExtraJson());
    }

    @Transactional(readOnly = true)
    public PaginaNotificacionesResponse obtenerNotificacionesUsuario(
            Integer idUsuario,
            String estado,
            int pagina,
            int tamanio) {
        var pageable = PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<Notificacion> page = switch (normalizarFiltro(estado)) {
            case "LEIDAS" -> notificacionRepository.buscarLeidasPorUsuario(idUsuario, pageable);
            case "NO_LEIDAS" -> notificacionRepository.buscarNoLeidasPorUsuario(idUsuario, pageable);
            default -> notificacionRepository.buscarPorUsuario(idUsuario, pageable);
        };

        long totalNoLeidas = notificacionRepository.countByUsuarioIdAndLeidoFalse(idUsuario);
        List<NotificacionResponse> contenido = page.getContent().stream()
                .map(this::mapearRespuesta)
                .toList();

        return new PaginaNotificacionesResponse(
                contenido,
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.hasNext(),
                totalNoLeidas);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Integer idUsuario) {
        return notificacionRepository.countByUsuarioIdAndLeidoFalse(idUsuario);
    }

    @Transactional
    public NotificacionResponse marcarLeida(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + id));
        notificacion.setLeido(true);
        return mapearRespuesta(notificacionRepository.save(notificacion));
    }

    @Transactional
    public boolean marcarTodasLeidas(Integer idUsuario) {
        notificacionRepository.marcarTodasLeidasPorUsuario(idUsuario);
        return true;
    }

    /**
     * Notificación automática al cancelar o modificar un viaje.
     */
    @Transactional
    public void notificarAutomaticaPorViaje(
            Integer idViaje,
            String tipo,
            String titulo,
            String mensaje,
            String datosExtraJson) {
        log.info(
                "[Notif] Notificación automática | idViaje={} tipo={} titulo=\"{}\"",
                idViaje,
                tipo,
                titulo);
        NotificacionViajeInput input = new NotificacionViajeInput(
                idViaje,
                tipo,
                titulo,
                mensaje,
                datosExtraJson);
        enviarPorViaje(input);
    }

    private List<NotificacionResponse> crearYEnviarNotificaciones(
            List<Integer> idsUsuarios,
            String tipo,
            String titulo,
            String mensaje,
            String datosExtraJson) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Notificacion> guardadas = new ArrayList<>();
        Map<Integer, Integer> noLeidasPorUsuario = new HashMap<>();

        for (Integer idUsuario : idsUsuarios) {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(usuario);
            notificacion.setTipo(tipo);
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje);
            notificacion.setFechaCreacion(ahora);
            notificacion.setLeido(false);
            notificacion.setDatosExtraJson(datosExtraJson);
            guardadas.add(notificacionRepository.save(notificacion));

            long noLeidas = notificacionRepository.countByUsuarioIdAndLeidoFalse(idUsuario) + 1;
            noLeidasPorUsuario.put(idUsuario, (int) noLeidas);
        }

        List<NotificacionResponse> respuestas = guardadas.stream()
                .map(this::mapearRespuesta)
                .toList();

        List<PushMessage> mensajesPush = construirMensajesPush(
                guardadas,
                idsUsuarios,
                tipo,
                titulo,
                mensaje,
                datosExtraJson,
                noLeidasPorUsuario);

        log.info(
                "[Notif] Notificaciones persistidas | tipo={} guardadas={} pushProgramados={}",
                tipo,
                guardadas.size(),
                mensajesPush.size());

        if ("EMERGENCIA_RUTA".equals(tipo)) {
            log.warn(
                    "[Notif] ALERTA EMERGENCIA enviada | destinatarios={} push={} titulo=\"{}\"",
                    guardadas.size(),
                    mensajesPush.size(),
                    titulo);
        }

        registrarEnvioPostCommit(mensajesPush);
        return respuestas;
    }

    private List<PushMessage> construirMensajesPush(
            List<Notificacion> notificaciones,
            List<Integer> idsUsuarios,
            String tipo,
            String titulo,
            String mensaje,
            String datosExtraJson,
            Map<Integer, Integer> noLeidasPorUsuario) {
        List<DispositivoPush> dispositivos = dispositivoPushRepository
                .findByUsuarioIdInAndActivoTrue(idsUsuarios);

        Map<Integer, Notificacion> notificacionPorUsuario = new HashMap<>();
        for (Notificacion notificacion : notificaciones) {
            notificacionPorUsuario.put(notificacion.getUsuario().getId(), notificacion);
        }

        List<PushMessage> mensajes = new ArrayList<>();
        Set<String> tokensUsados = new HashSet<>();

        for (DispositivoPush dispositivo : dispositivos) {
            if (!tokensUsados.add(dispositivo.getToken())) {
                continue;
            }

            Integer idUsuario = dispositivo.getUsuario().getId();
            Notificacion notificacion = notificacionPorUsuario.get(idUsuario);
            if (notificacion == null) {
                continue;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("tipo", tipo);
            data.put("idNotificacion", notificacion.getId());
            if (datosExtraJson != null && !datosExtraJson.isBlank()) {
                data.put("datosExtraJson", datosExtraJson);
            }

            mensajes.add(PushMessage.builder()
                    .to(dispositivo.getToken())
                    .title(titulo)
                    .body(mensaje)
                    .sound("default")
                    .badge(noLeidasPorUsuario.getOrDefault(idUsuario, 1))
                    .channelId("viajes")
                    .priority("high")
                    .data(data)
                    .build());
        }

        return mensajes;
    }

    private void registrarEnvioPostCommit(List<PushMessage> mensajesPush) {
        if (mensajesPush.isEmpty()) {
            log.info("[Notif] Sin dispositivos push activos; solo persistencia en bandeja");
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pushNotificationService.enviarAsync(mensajesPush);
                }
            });
        } else {
            pushNotificationService.enviarAsync(mensajesPush);
        }
    }

    private NotificacionResponse mapearRespuesta(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getId())
                .idUsuario(notificacion.getUsuario().getId())
                .tipo(notificacion.getTipo())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .fechaCreacion(notificacion.getFechaCreacion().toString())
                .leido(notificacion.isLeido())
                .datosExtraJson(notificacion.getDatosExtraJson())
                .build();
    }

    private void validarInputViaje(NotificacionViajeInput input) {
        if (input == null || input.getIdViaje() == null) {
            throw new IllegalArgumentException("idViaje es obligatorio");
        }
        if (input.getTitulo() == null || input.getTitulo().isBlank()) {
            throw new IllegalArgumentException("titulo es obligatorio");
        }
        if (input.getMensaje() == null || input.getMensaje().isBlank()) {
            throw new IllegalArgumentException("mensaje es obligatorio");
        }
        if (input.getTipo() == null || input.getTipo().isBlank()) {
            throw new IllegalArgumentException("tipo es obligatorio");
        }
    }

    private void validarInputUsuarios(NotificacionUsuarioInput input) {
        if (input == null || input.getIdsUsuario() == null || input.getIdsUsuario().isEmpty()) {
            throw new IllegalArgumentException("idsUsuario es obligatorio");
        }
        if (input.getTitulo() == null || input.getTitulo().isBlank()) {
            throw new IllegalArgumentException("titulo es obligatorio");
        }
        if (input.getMensaje() == null || input.getMensaje().isBlank()) {
            throw new IllegalArgumentException("mensaje es obligatorio");
        }
        if (input.getTipo() == null || input.getTipo().isBlank()) {
            throw new IllegalArgumentException("tipo es obligatorio");
        }
    }

    private void validarTipo(String tipo) {
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de notificación inválido: " + tipo);
        }
    }

    private String normalizarFiltro(String estado) {
        if (estado == null || estado.isBlank()) {
            return "TODAS";
        }
        return estado.trim().toUpperCase(Locale.ROOT);
    }

    private List<Integer> buscarDestinatariosPorViaje(Integer idViaje, String tipo) {
        if ("DOCUMENTACION_FALTANTE".equals(tipo)) {
            return reservaRepository.buscarIdsUsuariosConfirmadosPorViaje(idViaje);
        }
        return reservaRepository.buscarIdsUsuariosActivosPorViaje(idViaje);
    }
}

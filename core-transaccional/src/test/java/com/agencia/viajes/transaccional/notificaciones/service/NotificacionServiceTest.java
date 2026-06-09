package com.agencia.viajes.transaccional.notificaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionUsuarioInput;
import com.agencia.viajes.transaccional.notificaciones.dto.NotificacionViajeInput;
import com.agencia.viajes.transaccional.notificaciones.model.Notificacion;
import com.agencia.viajes.transaccional.notificaciones.push.PushNotificationService;
import com.agencia.viajes.transaccional.notificaciones.repository.DispositivoPushRepository;
import com.agencia.viajes.transaccional.notificaciones.repository.NotificacionRepository;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private DispositivoPushRepository dispositivoPushRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ViajeProgramadoRepository viajeProgramadoRepository;
    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private NotificacionService notificacionService;

    @Test
    void enviarPorViaje_creaNotificacionPorPasajeroActivo() {
        when(viajeProgramadoRepository.existsById(10)).thenReturn(true);
        when(reservaRepository.buscarIdsUsuariosActivosPorViaje(10)).thenReturn(List.of(7, 8));

        Usuario u7 = new Usuario();
        u7.setId(7);
        Usuario u8 = new Usuario();
        u8.setId(8);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(u7));
        when(usuarioRepository.findById(8)).thenReturn(Optional.of(u8));

        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(n.getUsuario().getId());
            return n;
        });
        when(notificacionRepository.countByUsuarioIdAndLeidoFalse(any())).thenReturn(0L);
        when(dispositivoPushRepository.findByUsuarioIdInAndActivoTrue(anyList())).thenReturn(List.of());

        NotificacionViajeInput input = new NotificacionViajeInput(
                10,
                "EMERGENCIA_RUTA",
                "Alerta",
                "Bloqueo en ruta",
                null);

        var result = notificacionService.enviarPorViaje(input);

        assertThat(result).hasSize(2);
        verify(reservaRepository).buscarIdsUsuariosActivosPorViaje(10);
        verify(reservaRepository, never()).buscarIdsUsuariosConfirmadosPorViaje(any());
        verify(notificacionRepository, org.mockito.Mockito.times(2)).save(any(Notificacion.class));
    }

    @Test
    void enviarPorViaje_documentacionFaltante_soloUsaPasajerosConfirmados() {
        when(viajeProgramadoRepository.existsById(10)).thenReturn(true);
        when(reservaRepository.buscarIdsUsuariosConfirmadosPorViaje(10)).thenReturn(List.of(7));

        Usuario u7 = new Usuario();
        u7.setId(7);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(u7));

        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(99);
            return n;
        });
        when(notificacionRepository.countByUsuarioIdAndLeidoFalse(7)).thenReturn(0L);
        when(dispositivoPushRepository.findByUsuarioIdInAndActivoTrue(List.of(7))).thenReturn(List.of());

        NotificacionViajeInput input = new NotificacionViajeInput(
                10,
                "DOCUMENTACION_FALTANTE",
                "Documentación pendiente",
                "Recuerda tu CI",
                "{\"motivo\":\"CI_PENDIENTE\"}");

        var result = notificacionService.enviarPorViaje(input);

        assertThat(result).hasSize(1);
        verify(reservaRepository).buscarIdsUsuariosConfirmadosPorViaje(10);
        verify(reservaRepository, never()).buscarIdsUsuariosActivosPorViaje(any());
    }

    @Test
    void enviarPorViaje_documentacionFaltante_sinConfirmados_devuelveListaVacia() {
        when(viajeProgramadoRepository.existsById(10)).thenReturn(true);
        when(reservaRepository.buscarIdsUsuariosConfirmadosPorViaje(10)).thenReturn(List.of());

        NotificacionViajeInput input = new NotificacionViajeInput(
                10,
                "DOCUMENTACION_FALTANTE",
                "Documentación pendiente",
                "Recuerda tu CI",
                null);

        var result = notificacionService.enviarPorViaje(input);

        assertThat(result).isEmpty();
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void enviarPorUsuarios_soportaLista() {
        Usuario u = new Usuario();
        u.setId(5);
        when(usuarioRepository.existsById(5)).thenReturn(true);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(u));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(99);
            return n;
        });
        when(notificacionRepository.countByUsuarioIdAndLeidoFalse(5)).thenReturn(0L);
        when(dispositivoPushRepository.findByUsuarioIdInAndActivoTrue(List.of(5))).thenReturn(List.of());

        NotificacionUsuarioInput input = new NotificacionUsuarioInput(
                List.of(5),
                "DOCUMENTACION_FALTANTE",
                "CI pendiente",
                "Recuerda tu CI",
                null);

        var result = notificacionService.enviarPorUsuarios(input);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipo()).isEqualTo("DOCUMENTACION_FALTANTE");
    }

    @Test
    void obtenerNotificacionesUsuario_respetaFiltroNoLeidas() {
        Notificacion n = new Notificacion();
        n.setId(1);
        Usuario u = new Usuario();
        u.setId(3);
        n.setUsuario(u);
        n.setTipo("CANCELACION");
        n.setTitulo("Cancelado");
        n.setMensaje("Msg");
        n.setFechaCreacion(LocalDateTime.now());
        n.setLeido(false);

        Page<Notificacion> page = new PageImpl<>(List.of(n));
        when(notificacionRepository.buscarNoLeidasPorUsuario(eq(3), any(Pageable.class))).thenReturn(page);
        when(notificacionRepository.countByUsuarioIdAndLeidoFalse(3)).thenReturn(1L);

        var result = notificacionService.obtenerNotificacionesUsuario(3, "NO_LEIDAS", 0, 20);

        assertThat(result.getContenido()).hasSize(1);
        assertThat(result.getTotalNoLeidas()).isEqualTo(1L);
    }

    @Test
    void marcarLeida_actualizaEstado() {
        Notificacion n = new Notificacion();
        n.setId(12);
        Usuario u = new Usuario();
        u.setId(4);
        n.setUsuario(u);
        n.setTipo("RETRASO");
        n.setTitulo("Retraso");
        n.setMensaje("25 min");
        n.setFechaCreacion(LocalDateTime.now());
        n.setLeido(false);

        when(notificacionRepository.findById(12)).thenReturn(Optional.of(n));
        when(notificacionRepository.save(n)).thenReturn(n);

        var result = notificacionService.marcarLeida(12);

        assertThat(result.getLeido()).isTrue();
        assertThat(n.isLeido()).isTrue();
    }
}

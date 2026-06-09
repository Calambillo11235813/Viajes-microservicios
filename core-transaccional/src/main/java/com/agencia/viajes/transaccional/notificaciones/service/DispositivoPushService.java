package com.agencia.viajes.transaccional.notificaciones.service;

import com.agencia.viajes.transaccional.notificaciones.dto.RegistrarDispositivoPushInput;
import com.agencia.viajes.transaccional.notificaciones.model.DispositivoPush;
import com.agencia.viajes.transaccional.notificaciones.repository.DispositivoPushRepository;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de tokens push registrados por dispositivo.
 */
@Service
@RequiredArgsConstructor
public class DispositivoPushService {

    private final DispositivoPushRepository dispositivoPushRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public boolean registrarToken(RegistrarDispositivoPushInput input) {
        validarInput(input);

        Usuario usuario = usuarioRepository.findById(input.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + input.getIdUsuario()));

        LocalDateTime ahora = LocalDateTime.now();
        DispositivoPush dispositivo = dispositivoPushRepository.findByToken(input.getToken())
                .orElseGet(DispositivoPush::new);

        dispositivo.setUsuario(usuario);
        dispositivo.setToken(input.getToken().trim());
        dispositivo.setPlataforma(normalizarPlataforma(input.getPlataforma()));
        dispositivo.setActivo(true);
        if (dispositivo.getFechaRegistro() == null) {
            dispositivo.setFechaRegistro(ahora);
        }
        dispositivo.setFechaUltimaActualizacion(ahora);

        dispositivoPushRepository.save(dispositivo);
        return true;
    }

    @Transactional
    public boolean desactivarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return dispositivoPushRepository.findByToken(token.trim())
                .map(dispositivo -> {
                    dispositivo.setActivo(false);
                    dispositivo.setFechaUltimaActualizacion(LocalDateTime.now());
                    dispositivoPushRepository.save(dispositivo);
                    return true;
                })
                .orElse(false);
    }

    private void validarInput(RegistrarDispositivoPushInput input) {
        if (input == null || input.getIdUsuario() == null) {
            throw new IllegalArgumentException("idUsuario es obligatorio");
        }
        if (input.getToken() == null || input.getToken().isBlank()) {
            throw new IllegalArgumentException("token es obligatorio");
        }
        if (input.getPlataforma() == null || input.getPlataforma().isBlank()) {
            throw new IllegalArgumentException("plataforma es obligatoria");
        }
    }

    private String normalizarPlataforma(String plataforma) {
        return plataforma.trim().toUpperCase(Locale.ROOT);
    }
}

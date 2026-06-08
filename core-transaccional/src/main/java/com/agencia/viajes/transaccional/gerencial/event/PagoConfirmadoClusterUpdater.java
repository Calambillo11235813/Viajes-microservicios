package com.agencia.viajes.transaccional.gerencial.event;

import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Escucha eventos de "PAGO_CONFIRMADO" en Redis y actualiza el cluster del usuario asíncronamente.
 */
@Component
@RequiredArgsConstructor
public class PagoConfirmadoClusterUpdater implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(PagoConfirmadoClusterUpdater.class);

    private final GerencialService gerencialService;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            JsonNode root = objectMapper.readTree(payload);
            
            if (root.has("eventType") && "PAGO_CONFIRMADO".equals(root.get("eventType").asText())) {
                if (root.has("idUsuario")) {
                    int idUsuario = root.get("idUsuario").asInt();
                    log.debug("[BI] Evento de pago detectado. Actualizando cluster para usuario: {}", idUsuario);
                    gerencialService.actualizarClusterDeUsuario(idUsuario);
                }
            }
        } catch (Exception e) {
            log.error("[BI] Error procesando evento de pago para actualizar cluster", e);
        }
    }
}

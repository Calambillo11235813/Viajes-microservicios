package com.agencia.viajes.transaccional.gerencial.scheduler;

import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada para resegmentar a todos los usuarios.
 */
@Component
@RequiredArgsConstructor
public class ActualizacionClustersJob {

    private final GerencialService gerencialService;

    @Scheduled(cron = "${bi.cron.segmentacion-masiva:0 0 3 * * ?}")
    public void ejecutarResegmentacionMasiva() {
        gerencialService.ejecutarResegmentacionMasiva();
    }
}

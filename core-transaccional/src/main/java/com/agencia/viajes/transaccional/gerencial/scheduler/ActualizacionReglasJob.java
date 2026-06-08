package com.agencia.viajes.transaccional.gerencial.scheduler;

import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada para actualizar diariamente las reglas de asociación.
 */
@Component
@RequiredArgsConstructor
public class ActualizacionReglasJob {

    private final GerencialService gerencialService;

    @Scheduled(cron = "${bi.cron.reglas-asociacion:0 0 2 * * ?}")
    public void ejecutarRefrescoReglas() {
        gerencialService.refrescarReglasDeAsociacion();
    }
}

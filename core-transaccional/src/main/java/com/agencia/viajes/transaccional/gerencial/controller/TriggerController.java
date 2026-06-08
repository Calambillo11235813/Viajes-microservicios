package com.agencia.viajes.transaccional.gerencial.controller;

import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TriggerController {
    private final GerencialService gerencialService;

    @GetMapping("/api/trigger-reglas")
    public String trigger() {
        gerencialService.refrescarReglasDeAsociacion();
        return "OK - Reglas de asociación refrescadas";
    }
}

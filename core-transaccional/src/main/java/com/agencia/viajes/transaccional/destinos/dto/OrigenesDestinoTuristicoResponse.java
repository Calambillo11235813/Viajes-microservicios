package com.agencia.viajes.transaccional.destinos.dto;

import java.util.List;

public record OrigenesDestinoTuristicoResponse(
        String departamento,
        List<String> origenes
) {}

package com.agencia.viajes.transaccional.destinos.graphql;

import com.agencia.viajes.transaccional.destinos.dto.DestinoViajesResponse;
import com.agencia.viajes.transaccional.destinos.service.DestinoTuristicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DestinoTuristicoResolver {

    private final DestinoTuristicoService destinoTuristicoService;

    @QueryMapping
    public DestinoViajesResponse buscarViajesPorDestinoTuristico(@Argument String nombreDestino) {
        return destinoTuristicoService.buscarViajesHaciaDestinoTuristico(nombreDestino);
    }
}

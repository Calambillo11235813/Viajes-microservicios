package com.agencia.viajes.transaccional.rutas.graphql;

import com.agencia.viajes.transaccional.rutas.dto.DestinoRecomendadoResponse;
import com.agencia.viajes.transaccional.rutas.dto.ReelTuristicoResponse;
import com.agencia.viajes.transaccional.rutas.service.InteligenciaArtificialService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InteligenciaArtificialResolver {

    private final InteligenciaArtificialService iaService;

    @QueryMapping
    public List<DestinoRecomendadoResponse> buscarDestinosPorImagen(@Argument String urlImagen) {
        return iaService.buscarDestinosPorImagen(urlImagen);
    }

    @QueryMapping
    public ReelTuristicoResponse generarReelTuristico(@Argument Integer idRuta) {
        return iaService.generarReelTuristico(idRuta);
    }
}

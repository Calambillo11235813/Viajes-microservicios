package com.agencia.viajes.transaccional.flotas.graphql;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.flotas.service.FlotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FlotaResolver {

    private final FlotaService flotaService;

    @QueryMapping
    public List<Flota> listarFlotas() {
        return flotaService.listarFlotas();
    }

    @MutationMapping
    public Flota crearFlota(@Argument String placa,
                            @Argument Integer capacidadTotalAsientos,
                            @Argument String tipoBus) {
        return flotaService.crearFlota(placa, capacidadTotalAsientos, tipoBus);
    }

    @MutationMapping
    public Flota actualizarFlota(@Argument Integer idBus,
                                 @Argument String placa,
                                 @Argument Integer capacidadTotalAsientos,
                                 @Argument String tipoBus) {
        return flotaService.actualizarFlota(idBus, placa, capacidadTotalAsientos, tipoBus);
    }

    @MutationMapping
    public Boolean eliminarFlota(@Argument Integer idBus) {
        return flotaService.eliminarFlota(idBus);
    }
}

package com.agencia.viajes.transaccional.rutas.resolver;

import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.rutas.service.RutasAdminService;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador de GraphQL para las operaciones administrativas de rutas y viajes (CU-09).
 */
@Controller
@RequiredArgsConstructor
public class RutasAdminResolver {

    private final RutasAdminService rutasAdminService;

    @QueryMapping
    public List<RutaDestino> listarRutas() {
        return rutasAdminService.listarRutas();
    }

    @QueryMapping
    public List<ViajeDisponibleResponse> listarViajesPorRuta(@Argument Integer idRuta) {
        return rutasAdminService.listarViajesPorRuta(idRuta);
    }

    @MutationMapping
    public RutaDestino crearRuta(
            @Argument String origen,
            @Argument String destino,
            @Argument Double duracion,
            @Argument Double precio,
            @Argument String categoria) {
        return rutasAdminService.crearRuta(origen, destino, BigDecimal.valueOf(duracion), BigDecimal.valueOf(precio), categoria);
    }

    @MutationMapping
    public RutaDestino actualizarRuta(
            @Argument Integer idRuta,
            @Argument String origen,
            @Argument String destino,
            @Argument Double duracion,
            @Argument Double precio,
            @Argument String categoria) {
        BigDecimal duracionDecimal = duracion != null ? BigDecimal.valueOf(duracion) : null;
        BigDecimal precioDecimal = precio != null ? BigDecimal.valueOf(precio) : null;
        return rutasAdminService.actualizarRuta(idRuta, origen, destino, duracionDecimal, precioDecimal, categoria);
    }

    @MutationMapping
    public Boolean eliminarRuta(@Argument Integer idRuta) {
        return rutasAdminService.eliminarRuta(idRuta);
    }

    @MutationMapping
    public ViajeDisponibleResponse programarViaje(
            @Argument Integer idRuta,
            @Argument Integer idBus,
            @Argument String fechaHoraSalida) {
        return rutasAdminService.programarViaje(idRuta, idBus, fechaHoraSalida);
    }

    @MutationMapping
    public ViajeDisponibleResponse actualizarViajeProgramado(
            @Argument Integer idViaje,
            @Argument Integer idBus,
            @Argument String fechaHoraSalida) {
        return rutasAdminService.actualizarViajeProgramado(idViaje, idBus, fechaHoraSalida);
    }

    @MutationMapping
    public Boolean cancelarViajeProgramado(@Argument Integer idViaje) {
        return rutasAdminService.cancelarViajeProgramado(idViaje);
    }
}

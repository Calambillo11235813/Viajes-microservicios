package com.agencia.viajes.transaccional.viajes.graphql;

import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.service.ViajeConsultaService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Consultas GraphQL relacionadas con viajes disponibles para el cliente.
 */
@Controller
@RequiredArgsConstructor
public class ViajeQueryResolver {

    private final ViajeConsultaService viajeConsultaService;

    /**
     * Retorna rutas y horarios programados según origen, destino y fecha.
     *
     * @param origen ciudad de salida.
     * @param destino ciudad de llegada.
     * @param fecha fecha de salida en formato ISO-8601.
     * @return viajes disponibles para los criterios solicitados.
     */
    @QueryMapping
    public List<ViajeDisponibleResponse> buscarRutasYHorariosDisponibles(
            @Argument String origen,
            @Argument String destino,
            @Argument String fecha) {
        return viajeConsultaService.buscarRutasYHorariosDisponibles(origen, destino, LocalDate.parse(fecha));
    }
}

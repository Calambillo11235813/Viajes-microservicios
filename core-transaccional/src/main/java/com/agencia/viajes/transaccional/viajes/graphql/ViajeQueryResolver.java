package com.agencia.viajes.transaccional.viajes.graphql;

import com.agencia.viajes.transaccional.viajes.dto.PaginaViajesResponse;
import com.agencia.viajes.transaccional.viajes.service.ViajeConsultaService;
import java.time.LocalDate;
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
     * Retorna rutas y horarios programados según origen, destino y fecha (paginados).
     */
    @QueryMapping
    public PaginaViajesResponse buscarRutasYHorariosDisponibles(
            @Argument String origen,
            @Argument String destino,
            @Argument String fecha,
            @Argument Integer idUsuario,
            @Argument Integer pagina,
            @Argument Integer tamanio) {
        int p = (pagina != null) ? pagina : 0;
        int t = (tamanio != null) ? tamanio : 15;
        return viajeConsultaService.buscarRutasYHorariosDisponibles(
                origen, destino, LocalDate.parse(fecha), idUsuario, p, t);
    }
}

package com.agencia.viajes.transaccional.pagos.service;

import com.agencia.viajes.transaccional.pagos.dto.ReporteVentasResponse;
import com.agencia.viajes.transaccional.pagos.dto.VentasPorFechaResponse;
import com.agencia.viajes.transaccional.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la generación de reportes financieros y de ventas (CU-10).
 */
@Service
@RequiredArgsConstructor
public class ReportesVentasService {

    private final PagoRepository pagoRepository;

    /**
     * Genera un reporte de ventas consolidadas en un rango de fechas.
     * Filtra únicamente pagos confirmados (exitosos).
     *
     * @param fechaInicioStr fecha de inicio en formato YYYY-MM-DD o YYYY-MM-DDTHH:mm:ss.
     * @param fechaFinStr fecha de fin en formato YYYY-MM-DD o YYYY-MM-DDTHH:mm:ss.
     * @return reporte consolidado de ventas.
     */
    @Transactional(readOnly = true)
    public ReporteVentasResponse generarReporteVentas(String fechaInicioStr, String fechaFinStr) {
        if (fechaInicioStr == null || fechaInicioStr.isBlank()) {
            throw new IllegalArgumentException("La fecha de inicio es requerida.");
        }
        if (fechaFinStr == null || fechaFinStr.isBlank()) {
            throw new IllegalArgumentException("La fecha de fin es requerida.");
        }

        LocalDateTime inicio = parseDateTime(fechaInicioStr, true);
        LocalDateTime fin = parseDateTime(fechaFinStr, false);

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        List<Object[]> rows = pagoRepository.findVentasAgrupadasPorFecha(inicio, fin);
        List<VentasPorFechaResponse> detalles = new ArrayList<>();

        BigDecimal montoTotal = BigDecimal.ZERO;
        int cantidadTotalPagos = 0;

        for (Object[] row : rows) {
            if (row == null || row.length < 3 || row[0] == null) {
                continue;
            }
            String fecha = row[0].toString();
            BigDecimal montoDia = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            int cantidadPagosDia = row[2] != null ? ((Number) row[2]).intValue() : 0;

            detalles.add(new VentasPorFechaResponse(fecha, montoDia, cantidadPagosDia));

            montoTotal = montoTotal.add(montoDia);
            cantidadTotalPagos += cantidadPagosDia;
        }

        // Cálculo mock de ocupación de flota para el KPI (en un entorno real esto calcularía Boletos Pagados / Capacidad Total)
        Double ocupacionFlota = cantidadTotalPagos > 0 ? 82.5 : 0.0;

        return new ReporteVentasResponse(
                montoTotal,
                cantidadTotalPagos,
                ocupacionFlota,
                fechaInicioStr,
                fechaFinStr,
                detalles
        );
    }

    private LocalDateTime parseDateTime(String value, boolean isStart) {
        try {
            String trimmed = value.trim();
            if (trimmed.contains("T")) {
                return LocalDateTime.parse(trimmed);
            } else {
                LocalDate date = LocalDate.parse(trimmed);
                return isStart ? date.atStartOfDay() : date.atTime(23, 59, 59, 999999999);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Formato de fecha inválido. Utilice YYYY-MM-DD o YYYY-MM-DDTHH:mm:ss"
            );
        }
    }
}

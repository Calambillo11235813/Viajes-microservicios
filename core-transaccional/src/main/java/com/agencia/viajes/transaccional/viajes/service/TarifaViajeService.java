package com.agencia.viajes.transaccional.viajes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Calcula la tarifa final visible al cliente según el servicio del bus.
 */
@Service
public class TarifaViajeService {

    private static final BigDecimal FACTOR_NORMAL = new BigDecimal("1.00");
    private static final BigDecimal FACTOR_SEMI_LECHO = new BigDecimal("1.15");
    private static final BigDecimal FACTOR_LECHO = new BigDecimal("1.30");
    private static final BigDecimal FACTOR_CAMA = new BigDecimal("1.45");

    public BigDecimal calcularPrecioPorServicio(BigDecimal precioBase, String tipoBus) {
        if (precioBase == null) {
            throw new IllegalArgumentException("El precio base no puede ser nulo.");
        }

        BigDecimal factor = obtenerFactor(tipoBus);
        return precioBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerFactor(String tipoBus) {
        if (tipoBus == null || tipoBus.isBlank()) {
            return FACTOR_NORMAL;
        }

        String normalizado = tipoBus.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");

        return switch (normalizado) {
            case "semilecho" -> FACTOR_SEMI_LECHO;
            case "lecho" -> FACTOR_LECHO;
            case "cama" -> FACTOR_CAMA;
            case "normal" -> FACTOR_NORMAL;
            default -> FACTOR_NORMAL;
        };
    }
}

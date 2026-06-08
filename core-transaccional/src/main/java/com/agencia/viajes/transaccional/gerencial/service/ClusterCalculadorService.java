package com.agencia.viajes.transaccional.gerencial.service;

import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11EstadisticasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11EstadisticasResponse.ClusterInfoDTO;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de calcular las características de los usuarios a partir de 
 * las transacciones en PostgreSQL y asignar etiquetas automáticas a los clústeres.
 */
@Service
@RequiredArgsConstructor
public class ClusterCalculadorService {

    private final JdbcTemplate jdbcTemplate;
    private final MotorIaGerencialClient motorIaGerencialClient;

    /**
     * Calcula las 4 características necesarias para CU11: total_gastado, num_reservas,
     * rutas_distintas, y promedio_pasajeros.
     *
     * @param idUsuario ID del usuario
     * @return Map con las características
     */
    public Map<String, Object> calcularCaracteristicasUsuario(Integer idUsuario) {
        String sql = """
            SELECT
                COALESCE(SUM(r.monto_total_pagado), 0) as total_gastado,
                COUNT(r.id_reserva) as num_reservas,
                COUNT(DISTINCT v.id_ruta) as rutas_distintas,
                COALESCE(AVG(r.cantidad_pasajeros), 0) as promedio_pasajeros
            FROM RESERVA r
            JOIN VIAJE_PROGRAMADO v ON r.id_viaje = v.id_viaje
            WHERE r.id_usuario = ? AND r.estado_reserva <> 'CANCELADA'
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Map<String, Object> features = new HashMap<>();
            features.put("total_gastado", rs.getBigDecimal("total_gastado"));
            features.put("num_reservas", rs.getInt("num_reservas"));
            features.put("rutas_distintas", rs.getInt("rutas_distintas"));
            features.put("promedio_pasajeros", rs.getBigDecimal("promedio_pasajeros"));
            return features;
        }, idUsuario);
    }

    /**
     * Infiere las etiquetas de los clústeres basándose en el total_gastado de sus centroides.
     * El que menos gasta es "Económico", el medio es "Moderado", el que más gasta es "Premium".
     *
     * @return Map donde la clave es el clusterId y el valor es la etiqueta ("Económico", etc.)
     */
    public Map<Integer, String> inferirEtiquetasClusters() {
        CU11EstadisticasResponse estadisticas = motorIaGerencialClient.obtenerEstadisticasClusters();
        Map<Integer, String> etiquetas = new HashMap<>();

        if (estadisticas == null || estadisticas.getClusters() == null || estadisticas.getClusters().isEmpty()) {
            return etiquetas; // Vacío en caso de error
        }

        List<ClusterInfoDTO> clusters = estadisticas.getClusters();

        // Si por alguna razón no son exactamente 3, asignamos genéricos,
        // pero el requerimiento establece que son 3.
        if (clusters.size() != 3) {
            for (int i = 0; i < clusters.size(); i++) {
                etiquetas.put(clusters.get(i).getCluster(), "Cluster " + i);
            }
            return etiquetas;
        }

        // Ordenar por total_gastado del centroide de menor a mayor
        clusters.sort(Comparator.comparing(c -> c.getCentroide().getTotal_gastado()));

        // El de menor gasto
        etiquetas.put(clusters.get(0).getCluster(), "Económico");
        // El del medio
        etiquetas.put(clusters.get(1).getCluster(), "Moderado");
        // El de mayor gasto
        etiquetas.put(clusters.get(2).getCluster(), "Premium");

        return etiquetas;
    }
}

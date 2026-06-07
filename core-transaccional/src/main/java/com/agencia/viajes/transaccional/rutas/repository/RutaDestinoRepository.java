package com.agencia.viajes.transaccional.rutas.repository;

import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de rutas de destino.
 */
public interface RutaDestinoRepository extends JpaRepository<RutaDestino, Integer> {

    @Query("""
            SELECT DISTINCT r.ciudadOrigen
            FROM RutaDestino r
            WHERE LOWER(r.ciudadDestino) = LOWER(:departamento)
              AND LOWER(r.ciudadOrigen) <> LOWER(:departamento)
              AND LOWER(r.ciudadOrigen) <> LOWER(:nombreTuristico)
              AND LOWER(r.ciudadOrigen) <> LOWER(:nombreConEspacios)
            ORDER BY r.ciudadOrigen ASC
            """)
    List<String> findOrigenesDisponiblesHaciaDestino(
            @Param("departamento") String departamento,
            @Param("nombreTuristico") String nombreTuristico,
            @Param("nombreConEspacios") String nombreConEspacios);
}

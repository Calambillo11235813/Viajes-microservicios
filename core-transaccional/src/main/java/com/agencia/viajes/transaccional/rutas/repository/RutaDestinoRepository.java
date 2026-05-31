package com.agencia.viajes.transaccional.rutas.repository;

import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de rutas de destino.
 */
public interface RutaDestinoRepository extends JpaRepository<RutaDestino, Integer> {
}

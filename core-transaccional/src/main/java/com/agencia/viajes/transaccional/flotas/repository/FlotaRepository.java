package com.agencia.viajes.transaccional.flotas.repository;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlotaRepository extends JpaRepository<Flota, Integer> {
}

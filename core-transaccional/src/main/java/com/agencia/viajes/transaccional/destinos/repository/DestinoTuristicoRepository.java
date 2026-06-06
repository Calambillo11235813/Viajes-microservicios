package com.agencia.viajes.transaccional.destinos.repository;

import com.agencia.viajes.transaccional.destinos.model.DestinoTuristico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinoTuristicoRepository extends JpaRepository<DestinoTuristico, Integer> {

    Optional<DestinoTuristico> findByNombreTuristicoIgnoreCase(String nombreTuristico);
}

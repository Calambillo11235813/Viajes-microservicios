package com.agencia.viajes.transaccional.gerencial.repository;

import com.agencia.viajes.transaccional.gerencial.model.ReglaAsociacionCache;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReglaAsociacionCacheRepository extends JpaRepository<ReglaAsociacionCache, Integer> {
    
    List<ReglaAsociacionCache> findAllByOrderByLiftDesc();
    
    // Spring Data truncating would require native query or custom implementation,
    // we'll use a standard deleteAllInBatch for now to simulate truncate.
}

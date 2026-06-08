package com.agencia.viajes.transaccional.gerencial.repository;

import com.agencia.viajes.transaccional.gerencial.model.DashboardKpiSnapshot;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardKpiSnapshotRepository extends JpaRepository<DashboardKpiSnapshot, Integer> {
    
    Optional<DashboardKpiSnapshot> findByFechaSnapshot(LocalDate fechaSnapshot);
    
    Optional<DashboardKpiSnapshot> findTopByOrderByFechaSnapshotDesc();
}

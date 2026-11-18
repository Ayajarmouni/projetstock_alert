package com.stockalert.alertes.repository;

import com.stockalert.alertes.entite.StockAlert;
import com.stockalert.alertes.enums.StatutAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByStatut(StatutAlerte statut);

    boolean existsByThresholdId(Long thresholdId);

    boolean existsByThresholdIdAndStatut(Long thresholdId, StatutAlerte statut);
}

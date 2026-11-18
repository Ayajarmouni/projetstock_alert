package com.stockalert.alertes.repository;

import com.stockalert.alertes.entite.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    boolean existsByProductIdAndEntrepotIgnoreCase(Long productId, String entrepot);

    boolean existsByProductIdAndEntrepotIgnoreCaseAndIdNot(Long productId, String entrepot, Long id);
}

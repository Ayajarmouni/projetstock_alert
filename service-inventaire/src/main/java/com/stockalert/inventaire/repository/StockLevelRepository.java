package com.stockalert.inventaire.repository;

import com.stockalert.inventaire.entite.StockLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    List<StockLevel> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    boolean existsByProductIdAndEntrepotIgnoreCase(Long productId, String entrepot);

    boolean existsByProductIdAndEntrepotIgnoreCaseAndIdNot(Long productId, String entrepot, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockLevel s where s.id = :id")
    Optional<StockLevel> findPourModification(@Param("id") Long id);
}

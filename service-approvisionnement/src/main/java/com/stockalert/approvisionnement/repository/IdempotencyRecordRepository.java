package com.stockalert.approvisionnement.repository;

import com.stockalert.approvisionnement.entite.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByCle(String cle);

    void deleteByPurchaseOrderId(Long purchaseOrderId);
}

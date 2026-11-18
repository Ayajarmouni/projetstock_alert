package com.stockalert.approvisionnement.repository;

import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByStatut(StatutCommande statut);

    boolean existsBySupplierId(Long supplierId);
}

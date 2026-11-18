package com.stockalert.approvisionnement.repository;

import com.stockalert.approvisionnement.entite.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}

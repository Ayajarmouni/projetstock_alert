package com.stockalert.inventaire.repository;

import com.stockalert.inventaire.entite.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCaseAndIdNot(String reference, Long id);
}

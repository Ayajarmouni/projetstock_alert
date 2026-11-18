package com.stockalert.approvisionnement.dto;

import com.stockalert.approvisionnement.enums.StatutCommande;

import java.time.LocalDate;

public record PurchaseOrderDto(
        Long id,
        Long supplierId,
        Long productId,
        String entrepot,
        Integer quantiteCommandee,
        StatutCommande statut,
        LocalDate date) {
}

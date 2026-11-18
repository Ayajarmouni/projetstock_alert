package com.stockalert.alertes.dto;

import com.stockalert.alertes.enums.StatutAlerte;

public record StockAlertDto(
        Long id,
        Long thresholdId,
        Long productId,
        String entrepot,
        Integer niveauDetecte,
        StatutAlerte statut) {
}

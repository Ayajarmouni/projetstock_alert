package com.stockalert.alertes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Création d'une alerte : produit et entrepôt sont repris de la règle de seuil. */
public record StockAlertRequestDto(
        @NotNull(message = "thresholdId est obligatoire")
        @Positive(message = "thresholdId doit être positif")
        Long thresholdId,
        @NotNull(message = "Le niveau détecté est obligatoire")
        @PositiveOrZero(message = "Le niveau détecté doit être positif ou nul")
        Integer niveauDetecte) {
}

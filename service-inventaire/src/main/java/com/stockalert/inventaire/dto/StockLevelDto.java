package com.stockalert.inventaire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StockLevelDto(
        Long id,
        @NotNull(message = "productId est obligatoire")
        @Positive(message = "productId doit être positif")
        Long productId,
        @NotBlank(message = "L'entrepôt est obligatoire")
        @Size(max = 100, message = "L'entrepôt ne doit pas dépasser 100 caractères")
        String entrepot,
        @NotNull(message = "La quantité est obligatoire")
        @PositiveOrZero(message = "La quantité doit être positive ou nulle")
        Integer quantite,
        @NotNull(message = "Le seuil d'alerte est obligatoire")
        @PositiveOrZero(message = "Le seuil d'alerte doit être positif ou nul")
        Integer seuilAlerte) {
}

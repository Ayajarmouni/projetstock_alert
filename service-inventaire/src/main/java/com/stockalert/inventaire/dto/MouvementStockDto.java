package com.stockalert.inventaire.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corps d'une entrée ou d'une sortie de stock. */
public record MouvementStockDto(
        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être strictement positive")
        Integer quantite) {
}

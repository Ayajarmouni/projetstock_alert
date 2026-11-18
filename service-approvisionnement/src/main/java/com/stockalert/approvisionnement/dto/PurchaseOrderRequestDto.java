package com.stockalert.approvisionnement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Données saisies pour créer/modifier une commande (statut et date sont gérés par le service). */
public record PurchaseOrderRequestDto(
        @NotNull(message = "supplierId est obligatoire")
        @Positive(message = "supplierId doit être positif")
        Long supplierId,
        @NotNull(message = "productId est obligatoire")
        @Positive(message = "productId doit être positif")
        Long productId,
        @NotBlank(message = "L'entrepôt est obligatoire")
        @Size(max = 100, message = "L'entrepôt ne doit pas dépasser 100 caractères")
        String entrepot,
        @NotNull(message = "La quantité commandée est obligatoire")
        @Positive(message = "La quantité commandée doit être strictement positive")
        Integer quantiteCommandee) {
}

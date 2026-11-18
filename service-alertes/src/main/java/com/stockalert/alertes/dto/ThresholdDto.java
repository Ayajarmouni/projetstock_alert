package com.stockalert.alertes.dto;

import com.stockalert.alertes.enums.Canal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ThresholdDto(
        Long id,
        @NotNull(message = "productId est obligatoire")
        @Positive(message = "productId doit être positif")
        Long productId,
        @NotNull(message = "supplierId est obligatoire")
        @Positive(message = "supplierId doit être positif")
        Long supplierId,
        @NotBlank(message = "L'entrepôt est obligatoire")
        @Size(max = 100, message = "L'entrepôt ne doit pas dépasser 100 caractères")
        String entrepot,
        @NotNull(message = "Le seuil minimum est obligatoire")
        @PositiveOrZero(message = "Le seuil minimum doit être positif ou nul")
        Integer seuilMin,
        @NotNull(message = "La quantité de réapprovisionnement est obligatoire")
        @Positive(message = "La quantité de réapprovisionnement doit être strictement positive")
        Integer quantiteReappro,
        @NotNull(message = "Le canal est obligatoire (EMAIL, SMS ou API)")
        Canal canal) {
}

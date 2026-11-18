package com.stockalert.approvisionnement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SupplierDto(
        Long id,
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
        String nom,
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
        String email,
        @NotNull(message = "Le délai de livraison est obligatoire")
        @Positive(message = "Le délai de livraison doit être strictement positif (en jours)")
        Integer delaiLivraisonJours) {
}

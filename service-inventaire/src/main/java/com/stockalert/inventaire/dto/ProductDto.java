package com.stockalert.inventaire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductDto(
        Long id,
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
        String nom,
        @NotBlank(message = "La référence est obligatoire")
        @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
        String reference,
        @NotBlank(message = "La catégorie est obligatoire")
        @Size(max = 255, message = "La catégorie ne doit pas dépasser 255 caractères")
        String categorie) {
}

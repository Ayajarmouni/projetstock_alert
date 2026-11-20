package com.stockalert.approvisionnement.dto.client;

/** Produit tel que renvoyé par service-inventaire (GET /products/{id}). */
public record ProduitDistantDto(Long id, String nom, String reference, String categorie) {
}

package com.stockalert.approvisionnement.dto.client;

/** Niveau de stock tel que renvoyé par service-inventaire (GET /stock-levels). */
public record StockDistantDto(Long id, Long productId, String entrepot, Integer quantite, Integer seuilAlerte) {
}

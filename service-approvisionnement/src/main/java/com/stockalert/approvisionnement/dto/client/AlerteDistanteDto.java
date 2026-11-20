package com.stockalert.approvisionnement.dto.client;

/** Alerte telle que renvoyée par service-alertes (GET /stock-alerts). */
public record AlerteDistanteDto(Long id, Long thresholdId, Long productId, String entrepot,
                                Integer niveauDetecte, String statut) {
}

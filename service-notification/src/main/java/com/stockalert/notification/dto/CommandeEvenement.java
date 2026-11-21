package com.stockalert.notification.dto;

/**
 * Contrat JSON reçu de service-approvisionnement (même noms de champs que le producteur). Les champs inconnus sont
 * ignorés, ce qui permet au producteur d'ajouter des champs sans casser ce consommateur.
 */
public record CommandeEvenement(
        String eventId,
        String type,
        String occurredAt,
        Long purchaseOrderId,
        String statut,
        String date,
        Long supplierId,
        String supplierNom,
        String supplierEmail,
        int delaiLivraisonJours,
        Long productId,
        String produitNom,
        String entrepot,
        int quantiteCommandee) {

    public static final String EMISE = "PURCHASE_ORDER_EMISE";
    public static final String LIVREE = "PURCHASE_ORDER_LIVREE";
}

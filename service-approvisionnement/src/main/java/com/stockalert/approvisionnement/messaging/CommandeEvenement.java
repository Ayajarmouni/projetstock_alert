package com.stockalert.approvisionnement.messaging;

/**
 * Contrat JSON de l'événement publié sur RabbitMQ lorsqu'une commande d'achat change d'état (validée en base).
 * Toutes les dates sont des chaînes ISO-8601 : le contrat reste indépendant de la configuration Jackson des services.
 * Le même contrat (même noms de champs) est lu par service-notification.
 *
 * @param eventId             identifiant unique de l'événement (sert à ignorer les doublons côté consommateur)
 * @param type                {@link #EMISE} ou {@link #LIVREE}
 * @param occurredAt          horodatage de la validation de la transaction
 * @param produitNom          nom du produit si service-inventaire a pu être interrogé, sinon {@code null}
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

    /** Clé de routage sur l'échange {@code stockalert.events} : {@code purchase-order.emise} / {@code purchase-order.livree}. */
    public String routingKey() {
        return RabbitConfig.ROUTING_PREFIX + (EMISE.equals(type) ? "emise" : "livree");
    }

    public CommandeEvenement avecProduitNom(String nom) {
        return new CommandeEvenement(eventId, type, occurredAt, purchaseOrderId, statut, date, supplierId, supplierNom,
                supplierEmail, delaiLivraisonJours, productId, nom, entrepot, quantiteCommandee);
    }
}

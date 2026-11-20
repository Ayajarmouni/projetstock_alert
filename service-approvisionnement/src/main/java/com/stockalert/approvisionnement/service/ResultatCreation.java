package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.entite.PurchaseOrder;

/** Résultat d'une création : {@code rejouee} vaut true si la clé d'idempotence avait déjà servi (aucune nouvelle commande). */
public record ResultatCreation(PurchaseOrder commande, boolean rejouee) {
}

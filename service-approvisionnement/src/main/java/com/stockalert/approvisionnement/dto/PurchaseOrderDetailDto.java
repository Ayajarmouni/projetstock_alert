package com.stockalert.approvisionnement.dto;

import com.stockalert.approvisionnement.dto.client.AlerteDistanteDto;
import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import com.stockalert.approvisionnement.enums.StatutCommande;

import java.time.LocalDate;
import java.util.List;

/**
 * Consultation enrichie d'une commande : données locales + fournisseur (local) + produit, stock et alerte ouverte
 * récupérés via OpenFeign. Si un service distant ne répond pas, le champ correspondant vaut null et la raison
 * est indiquée dans {@code avertissements} (la commande reste consultable).
 */
public record PurchaseOrderDetailDto(
        Long id,
        Long supplierId,
        Long productId,
        String entrepot,
        Integer quantiteCommandee,
        StatutCommande statut,
        LocalDate date,
        SupplierDto fournisseur,
        ProduitDistantDto produit,
        StockDistantDto stock,
        AlerteDistanteDto alerteOuverte,
        List<String> avertissements) {
}

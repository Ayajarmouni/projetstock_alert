package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceDistanteIntrouvableException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;

/**
 * Contrôles métier effectués AVANT l'enregistrement d'une commande d'achat, auprès de service-inventaire :
 * 1. le produit existe ; 2. le produit est stocké dans l'entrepôt ; 3. le stock est bas (quantité <= seuil d'alerte).
 */
@Service
public class ControleCommandeService {
    private final ServicesDistantsGateway distants;

    public ControleCommandeService(ServicesDistantsGateway distants) {
        this.distants = distants;
    }

    /** @return le niveau de stock contrôlé */
    public StockDistantDto verifierReapprovisionnement(Long productId, String entrepot) {
        try {
            distants.produit(productId);
        } catch (RessourceDistanteIntrouvableException e) {
            throw new RessourceIntrouvableException("Produit introuvable avec l'id " + productId
                    + " (vérifié auprès de service-inventaire)");
        }
        StockDistantDto stock = distants.stocks(productId, entrepot).stream()
                .filter(s -> s.entrepot() != null && s.entrepot().equalsIgnoreCase(entrepot))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun niveau de stock pour le produit "
                        + productId + " dans l'entrepôt '" + entrepot + "'"));
        if (stock.quantite() > stock.seuilAlerte()) {
            throw new ConflitException("Réapprovisionnement refusé : le stock du produit " + productId
                    + " dans l'entrepôt '" + entrepot + "' est suffisant (quantité " + stock.quantite()
                    + " > seuil d'alerte " + stock.seuilAlerte() + ")");
        }
        return stock;
    }
}

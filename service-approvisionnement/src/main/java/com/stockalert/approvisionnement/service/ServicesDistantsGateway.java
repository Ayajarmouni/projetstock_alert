package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.client.AlertesClient;
import com.stockalert.approvisionnement.client.InventaireClient;
import com.stockalert.approvisionnement.dto.client.AlerteDistanteDto;
import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import com.stockalert.approvisionnement.exception.ServiceDistantException;
import com.stockalert.approvisionnement.exception.ServiceDistantIndisponibleException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Point d'accès unique aux autres microservices. Toute erreur de communication non déjà typée par l'ErrorDecoder
 * (connexion refusée, délai dépassé...) est convertie en {@link ServiceDistantIndisponibleException}.
 */
@Component
public class ServicesDistantsGateway {
    private static final Logger log = LoggerFactory.getLogger(ServicesDistantsGateway.class);
    static final String INVENTAIRE = "service-inventaire";
    static final String ALERTES = "service-alertes";

    private final InventaireClient inventaireClient;
    private final AlertesClient alertesClient;

    public ServicesDistantsGateway(InventaireClient inventaireClient, AlertesClient alertesClient) {
        this.inventaireClient = inventaireClient;
        this.alertesClient = alertesClient;
    }

    /** @throws com.stockalert.approvisionnement.exception.RessourceDistanteIntrouvableException si le produit n'existe pas */
    public ProduitDistantDto produit(Long productId) {
        return appeler(INVENTAIRE, "GET produit " + productId, () -> inventaireClient.trouverProduit(productId));
    }

    public List<StockDistantDto> stocks(Long productId, String entrepot) {
        return appeler(INVENTAIRE, "GET stocks produit " + productId + " / " + entrepot,
                () -> inventaireClient.listerStocks(productId, entrepot));
    }

    public List<AlerteDistanteDto> alertesOuvertes(Long productId) {
        return appeler(ALERTES, "GET alertes ouvertes produit " + productId,
                () -> alertesClient.listerAlertes("OUVERTE", productId));
    }

    private <T> T appeler(String service, String operation, Supplier<T> appel) {
        long debut = System.currentTimeMillis();
        try {
            T resultat = appel.get();
            log.info("Appel Feign {} -> {} : OK en {} ms", service, operation, System.currentTimeMillis() - debut);
            return resultat;
        } catch (ServiceDistantException e) {
            log.warn("Appel Feign {} -> {} : {} ({})", service, operation, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("Appel Feign {} -> {} : échec de communication ({})", service, operation, e.getMessage());
            throw new ServiceDistantIndisponibleException(service, "injoignable ou délai dépassé", e);
        }
    }
}

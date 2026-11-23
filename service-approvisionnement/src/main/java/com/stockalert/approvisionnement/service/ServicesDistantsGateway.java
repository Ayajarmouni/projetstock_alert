
package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.client.AlertesClient;
import com.stockalert.approvisionnement.client.InventaireClient;

import com.stockalert.approvisionnement.dto.client.AlerteDistanteDto;
import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;

import com.stockalert.approvisionnement.exception.ServiceDistantException;
import com.stockalert.approvisionnement.exception.ServiceDistantIndisponibleException;

import feign.FeignException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class ServicesDistantsGateway {

    private static final Logger log =
            LoggerFactory.getLogger(ServicesDistantsGateway.class);

    private static final String INVENTAIRE = "service-inventaire";
    private static final String ALERTES = "service-alertes";

    private final InventaireClient inventaireClient;
    private final AlertesClient alertesClient;

    public ServicesDistantsGateway(
            InventaireClient inventaireClient,
            AlertesClient alertesClient) {

        this.inventaireClient = inventaireClient;
        this.alertesClient = alertesClient;
    }

    // ==========================================
    // APPEL 1 : PRODUIT (INVENTAIRE)
    // ==========================================

    @Retry(name = "inventaire", fallbackMethod = "repliProduit")
    @CircuitBreaker(name = "inventaire")
    public ProduitDistantDto produit(Long productId) {

        return appeler(
                INVENTAIRE,
                "GET produit " + productId,
                () -> inventaireClient.trouverProduit(productId)
        );
    }

    // ==========================================
    // APPEL 2 : STOCKS (INVENTAIRE)
    // ==========================================

    @Retry(name = "inventaire", fallbackMethod = "repliStocks")
    @CircuitBreaker(name = "inventaire")
    public List<StockDistantDto> stocks(
            Long productId,
            String entrepot) {

        return appeler(
                INVENTAIRE,
                "GET stocks " + productId + " / " + entrepot,
                () -> inventaireClient.listerStocks(productId, entrepot)
        );
    }

    // ==========================================
    // APPEL 3 : ALERTES (SERVICE-ALERTES)
    // ==========================================

    @Retry(name = "alertes", fallbackMethod = "repliAlertes")
    @CircuitBreaker(name = "alertes")
    public List<AlerteDistanteDto> alertesOuvertes(Long productId) {

        return appeler(
                ALERTES,
                "GET alertes ouvertes " + productId,
                () -> alertesClient.listerAlertes("OUVERTE", productId)
        );
    }

    // ==========================================
    // FALLBACKS
    // ==========================================

    private ProduitDistantDto repliProduit(
            Long productId,
            Throwable erreur) {

        throw gererEchec(INVENTAIRE, erreur);
    }

    private List<StockDistantDto> repliStocks(
            Long productId,
            String entrepot,
            Throwable erreur) {

        throw gererEchec(INVENTAIRE, erreur);
    }

    private List<AlerteDistanteDto> repliAlertes(
            Long productId,
            Throwable erreur) {

        throw gererEchec(ALERTES, erreur);
    }

    // ==========================================
    // GESTION DES ERREURS
    // ==========================================

    private RuntimeException gererEchec(
            String service,
            Throwable erreur) {

        if (erreur instanceof ServiceDistantException ex) {
            return ex;
        }

        if (erreur instanceof CallNotPermittedException) {
            log.warn(
                    "Circuit Breaker OPEN : appels bloqués vers {}",
                    service
            );

            return new ServiceDistantIndisponibleException(
                    service,
                    "Circuit Breaker ouvert",
                    erreur
            );
        }

        if (erreur instanceof RuntimeException ex) {
            return ex;
        }

        return new ServiceDistantIndisponibleException(
                service,
                "Erreur de communication",
                erreur
        );
    }

    // ==========================================
    // EXECUTION DES APPELS FEIGN
    // ==========================================

    private <T> T appeler(
            String service,
            String operation,
            Supplier<T> action) {

        long debut = System.currentTimeMillis();

        try {
            T resultat = action.get();

            log.info(
                    "Appel Feign {} -> {} : OK en {} ms",
                    service,
                    operation,
                    System.currentTimeMillis() - debut
            );

            return resultat;

        } catch (ServiceDistantException ex) {

            log.warn(
                    "Appel Feign {} -> {} : {}",
                    service,
                    operation,
                    ex.getMessage()
            );

            throw ex;

        } catch (FeignException ex) {

            log.error(
                    "Échec Feign {} -> {} : {}",
                    service,
                    operation,
                    ex.getMessage()
            );

            throw new ServiceDistantIndisponibleException(
                    service,
                    "Connexion impossible ou délai dépassé",
                    ex
            );
        }
    }
}

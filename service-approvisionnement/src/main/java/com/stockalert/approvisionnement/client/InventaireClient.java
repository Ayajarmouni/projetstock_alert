package com.stockalert.approvisionnement.client;

import com.stockalert.approvisionnement.config.FeignConfiguration;
import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Client OpenFeign vers service-inventaire. Le nom du service est résolu via Eureka (load balancing côté client).
 * La propriété {@code clients.inventaire.url} permet de court-circuiter Eureka (tests locaux), vide par défaut.
 */
@FeignClient(name = "service-inventaire", url = "${clients.inventaire.url:}", configuration = FeignConfiguration.class)
public interface InventaireClient {

    @GetMapping("/products/{id}")
    ProduitDistantDto trouverProduit(@PathVariable("id") Long id);

    @GetMapping("/stock-levels")
    List<StockDistantDto> listerStocks(@RequestParam("productId") Long productId,
                                       @RequestParam("entrepot") String entrepot);
}

package com.stockalert.approvisionnement.client;

import com.stockalert.approvisionnement.config.FeignConfiguration;
import com.stockalert.approvisionnement.dto.client.AlerteDistanteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Client OpenFeign vers service-alertes (propriété de contournement : {@code clients.alertes.url}). */
@FeignClient(name = "service-alertes", url = "${clients.alertes.url:}", configuration = FeignConfiguration.class)
public interface AlertesClient {

    @GetMapping("/stock-alerts")
    List<AlerteDistanteDto> listerAlertes(@RequestParam("statut") String statut,
                                          @RequestParam("productId") Long productId);
}

package com.stockalert.approvisionnement.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuration propre aux clients Feign (volontairement NON annotée @Configuration : elle est référencée par
 * {@code @FeignClient(configuration = ...)} et ne doit pas être détectée par le scan de composants).
 * Les délais de connexion / lecture se règlent dans config-repo/service-approvisionnement.properties.
 */
public class FeignConfiguration {

    /** Traduit les réponses HTTP en erreur des services distants en exceptions métier typées. */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErreurDecoder();
    }

    /**
     * Nouvelle tentative uniquement sur les erreurs réseau (connexion refusée, délai dépassé) :
     * 2 essais au total, 200 ms d'attente. Les appels étant des GET, ils sont rejouables sans risque.
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(200, 500, 2);
    }
}

package com.stockalert.approvisionnement.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockalert.approvisionnement.exception.ReponseDistanteInattendueException;
import com.stockalert.approvisionnement.exception.RessourceDistanteIntrouvableException;
import com.stockalert.approvisionnement.exception.ServiceDistantIndisponibleException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.InputStream;

/**
 * Convertit une réponse d'erreur d'un service distant :
 * 404 -> ressource introuvable ; 5xx -> service indisponible ; autre 4xx -> réponse inattendue.
 * Le message est repris du corps JSON standard des services (champ {@code message}).
 */
public class FeignErreurDecoder implements ErrorDecoder {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String service = methodKey != null && methodKey.startsWith("AlertesClient") ? "service-alertes" : "service-inventaire";
        int status = response.status();
        String message = extraireMessage(response);
        if (status == 404) {
            return new RessourceDistanteIntrouvableException(service, message);
        }
        if (status >= 500) {
            return new ServiceDistantIndisponibleException(service,
                    "répond avec l'erreur " + status + " (" + message + ")", null);
        }
        return new ReponseDistanteInattendueException(service, status, message);
    }

    private String extraireMessage(Response response) {
        try {
            if (response.body() != null) {
                try (InputStream in = response.body().asInputStream()) {
                    JsonNode noeud = MAPPER.readTree(in);
                    if (noeud != null && noeud.hasNonNull("message")) {
                        return noeud.get("message").asText();
                    }
                }
            }
        } catch (Exception ignoree) {
            // corps absent ou non JSON : on retombe sur la raison HTTP
        }
        return response.reason() != null ? response.reason() : "sans détail";
    }
}

package com.stockalert.approvisionnement.exception;

/** Le service distant a répondu 404 : la ressource référencée n'existe pas chez lui. */
public class RessourceDistanteIntrouvableException extends ServiceDistantException {
    public RessourceDistanteIntrouvableException(String service, String message) {
        super(service, message, null);
    }
}

package com.stockalert.approvisionnement.exception;

/** Le service distant a répondu par une erreur 4xx autre que 404 : l'appel est incompatible avec son contrat. */
public class ReponseDistanteInattendueException extends ServiceDistantException {
    private final int statutDistant;

    public ReponseDistanteInattendueException(String service, int statutDistant, String detail) {
        super(service, "Réponse inattendue du service '" + service + "' (HTTP " + statutDistant + ") : " + detail, null);
        this.statutDistant = statutDistant;
    }

    public int getStatutDistant() {
        return statutDistant;
    }
}

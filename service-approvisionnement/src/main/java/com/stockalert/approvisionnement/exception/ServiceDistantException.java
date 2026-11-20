package com.stockalert.approvisionnement.exception;

/** Erreur survenue lors d'un appel vers un autre microservice. */
public abstract class ServiceDistantException extends RuntimeException {
    private final String service;

    protected ServiceDistantException(String service, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}

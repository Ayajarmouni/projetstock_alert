package com.stockalert.approvisionnement.exception;

/** Le service distant est injoignable (réseau, délai dépassé, aucune instance) ou répond en 5xx. */
public class ServiceDistantIndisponibleException extends ServiceDistantException {
    public ServiceDistantIndisponibleException(String service, String detail, Throwable cause) {
        super(service, "Le service '" + service + "' est indisponible : " + detail, cause);
    }
}

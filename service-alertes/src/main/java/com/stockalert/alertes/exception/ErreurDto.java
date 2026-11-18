package com.stockalert.alertes.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Corps JSON renvoyé pour toutes les erreurs de l'API. */
public record ErreurDto(
        LocalDateTime timestamp,
        int status,
        String erreur,
        String message,
        String chemin,
        List<String> details) {
}

package com.stockalert.notification.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Corps JSON renvoyé pour toutes les erreurs de l'API (même format que les autres services). */
public record ErreurDto(
        LocalDateTime timestamp,
        int status,
        String erreur,
        String message,
        String chemin,
        List<String> details) {
}

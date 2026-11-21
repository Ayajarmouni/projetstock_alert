package com.stockalert.notification.dto;

import java.time.LocalDateTime;

public record NotificationDto(Long id, String eventId, String type, Long purchaseOrderId, String destinataire,
                              String sujet, String statut, LocalDateTime dateEnvoi) {
}

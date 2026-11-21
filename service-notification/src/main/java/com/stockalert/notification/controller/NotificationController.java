package com.stockalert.notification.controller;

import com.stockalert.notification.dto.NotificationDto;
import com.stockalert.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Historique des emails envoyés (lecture seule) : permet de vérifier le traitement côté consommateur. */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> lister(
            @RequestParam(name = "purchaseOrderId", required = false) Long purchaseOrderId) {
        return ResponseEntity.ok(service.lister(purchaseOrderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.trouver(id));
    }
}

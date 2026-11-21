package com.stockalert.notification.entite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Trace d'un email envoyé. La contrainte d'unicité sur {@code eventId} garantit un seul email par événement. */
@Entity
@Table(name = "notification_log", uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(nullable = false)
    private String type;

    private Long purchaseOrderId;

    @Column(nullable = false)
    private String destinataire;

    @Column(nullable = false)
    private String sujet;

    @Column(nullable = false)
    private String statut;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    public NotificationLog() {
    }

    public NotificationLog(String eventId, String type, Long purchaseOrderId, String destinataire, String sujet,
                           String statut, LocalDateTime dateEnvoi) {
        this.eventId = eventId;
        this.type = type;
        this.purchaseOrderId = purchaseOrderId;
        this.destinataire = destinataire;
        this.sujet = sujet;
        this.statut = statut;
        this.dateEnvoi = dateEnvoi;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getType() { return type; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public String getDestinataire() { return destinataire; }
    public String getSujet() { return sujet; }
    public String getStatut() { return statut; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
}

package com.stockalert.approvisionnement.entite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Trace d'une création de commande effectuée avec une clé d'idempotence : la contrainte d'unicité sur {@code cle}
 * garantit qu'une même clé ne produit jamais deux commandes, même en cas de requêtes simultanées.
 */
@Entity
@Table(name = "idempotency_record", uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_cle", columnNames = "cle"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String cle;

    /** Empreinte SHA-256 du contenu de la requête : détecte la réutilisation d'une clé avec un corps différent. */
    @Column(nullable = false, length = 64)
    private String empreinte;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    public IdempotencyRecord() {
    }

    public IdempotencyRecord(String cle, String empreinte, Long purchaseOrderId) {
        this.cle = cle;
        this.empreinte = empreinte;
        this.purchaseOrderId = purchaseOrderId;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCle() {
        return cle;
    }

    public String getEmpreinte() {
        return empreinte;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}

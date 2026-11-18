package com.stockalert.alertes.entite;

import jakarta.persistence.*;
import com.stockalert.alertes.enums.StatutAlerte;

@Entity
@Table(name = "stock_alert")
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long thresholdId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String entrepot;

    @Column(nullable = false)
    private int niveauDetecte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAlerte statut;

    public StockAlert() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Long thresholdId) {
        this.thresholdId = thresholdId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getEntrepot() {
        return entrepot;
    }

    public void setEntrepot(String entrepot) {
        this.entrepot = entrepot;
    }

    public int getNiveauDetecte() {
        return niveauDetecte;
    }

    public void setNiveauDetecte(int niveauDetecte) {
        this.niveauDetecte = niveauDetecte;
    }

    public StatutAlerte getStatut() {
        return statut;
    }

    public void setStatut(StatutAlerte statut) {
        this.statut = statut;
    }
}

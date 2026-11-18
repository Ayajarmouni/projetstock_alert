package com.stockalert.alertes.entite;

import jakarta.persistence.*;
import com.stockalert.alertes.enums.Canal;

@Entity
@Table(name = "threshold", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "entrepot"}))
public class Threshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private String entrepot;

    @Column(nullable = false)
    private int seuilMin;

    @Column(nullable = false)
    private int quantiteReappro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Canal canal;

    public Threshold() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getEntrepot() {
        return entrepot;
    }

    public void setEntrepot(String entrepot) {
        this.entrepot = entrepot;
    }

    public int getSeuilMin() {
        return seuilMin;
    }

    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }

    public int getQuantiteReappro() {
        return quantiteReappro;
    }

    public void setQuantiteReappro(int quantiteReappro) {
        this.quantiteReappro = quantiteReappro;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }
}

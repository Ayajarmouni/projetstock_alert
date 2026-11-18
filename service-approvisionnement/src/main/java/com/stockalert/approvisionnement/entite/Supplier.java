package com.stockalert.approvisionnement.entite;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int delaiLivraisonJours;

    public Supplier() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDelaiLivraisonJours() {
        return delaiLivraisonJours;
    }

    public void setDelaiLivraisonJours(int delaiLivraisonJours) {
        this.delaiLivraisonJours = delaiLivraisonJours;
    }
}

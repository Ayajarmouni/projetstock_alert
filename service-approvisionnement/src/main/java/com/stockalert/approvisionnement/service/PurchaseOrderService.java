package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.enums.StatutCommande;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import com.stockalert.approvisionnement.repository.PurchaseOrderRepository;
import com.stockalert.approvisionnement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Unité de traitement du service : la commande d'achat.
 * Cycle de vie : BROUILLON -> EMISE -> LIVREE (aucun retour arrière).
 */
@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> lister(StatutCommande statut, Long supplierId) {
        List<PurchaseOrder> commandes = statut != null ? purchaseOrderRepository.findByStatut(statut)
                : purchaseOrderRepository.findAll();
        if (supplierId != null) {
            commandes = commandes.stream().filter(c -> supplierId.equals(c.getSupplierId())).toList();
        }
        return commandes;
    }

    @Transactional(readOnly = true)
    public PurchaseOrder trouver(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Commande d'achat introuvable avec l'id " + id));
    }

    /** Une nouvelle commande est toujours créée au statut BROUILLON, datée du jour. */
    @Transactional
    public PurchaseOrder creer(PurchaseOrder commande) {
        verifierFournisseur(commande.getSupplierId());
        commande.setId(null);
        commande.setStatut(StatutCommande.BROUILLON);
        commande.setDate(LocalDate.now());
        return purchaseOrderRepository.save(commande);
    }

    @Transactional
    public PurchaseOrder modifier(Long id, PurchaseOrder nouvelle) {
        PurchaseOrder commande = trouver(id);
        exigerStatut(commande, StatutCommande.BROUILLON, "modifiée");
        verifierFournisseur(nouvelle.getSupplierId());
        commande.setSupplierId(nouvelle.getSupplierId());
        commande.setProductId(nouvelle.getProductId());
        commande.setEntrepot(nouvelle.getEntrepot());
        commande.setQuantiteCommandee(nouvelle.getQuantiteCommandee());
        return purchaseOrderRepository.save(commande);
    }

    @Transactional
    public void supprimer(Long id) {
        PurchaseOrder commande = trouver(id);
        exigerStatut(commande, StatutCommande.BROUILLON, "supprimée");
        purchaseOrderRepository.delete(commande);
    }

    /** BROUILLON -> EMISE : la commande est transmise au fournisseur. */
    @Transactional
    public PurchaseOrder emettre(Long id) {
        PurchaseOrder commande = trouver(id);
        exigerStatut(commande, StatutCommande.BROUILLON, "émise");
        commande.setStatut(StatutCommande.EMISE);
        return purchaseOrderRepository.save(commande);
    }

    /** EMISE -> LIVREE : réception de la livraison. */
    @Transactional
    public PurchaseOrder livrer(Long id) {
        PurchaseOrder commande = trouver(id);
        exigerStatut(commande, StatutCommande.EMISE, "livrée");
        commande.setStatut(StatutCommande.LIVREE);
        // Jalon suivant : publication de l'événement Kafka "delivery-received" (mise à jour du stock, clôture de l'alerte).
        return purchaseOrderRepository.save(commande);
    }

    private void exigerStatut(PurchaseOrder commande, StatutCommande attendu, String action) {
        if (commande.getStatut() != attendu) {
            throw new ConflitException("La commande " + commande.getId() + " ne peut pas être " + action
                    + " : statut actuel " + commande.getStatut() + ", statut requis " + attendu);
        }
    }

    private void verifierFournisseur(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new RessourceIntrouvableException("Fournisseur introuvable avec l'id " + supplierId);
        }
    }
}

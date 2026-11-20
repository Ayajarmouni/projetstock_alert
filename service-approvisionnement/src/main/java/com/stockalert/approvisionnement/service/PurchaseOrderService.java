package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.entite.IdempotencyRecord;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.enums.StatutCommande;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import com.stockalert.approvisionnement.repository.IdempotencyRecordRepository;
import com.stockalert.approvisionnement.repository.PurchaseOrderRepository;
import com.stockalert.approvisionnement.repository.SupplierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;

/**
 * Unité de traitement du service : la commande d'achat.
 * Cycle de vie : BROUILLON -> EMISE -> LIVREE (aucun retour arrière).
 *
 * V4 : la création et la modification sont contrôlées auprès de service-inventaire (OpenFeign) et la création est
 * protégée par une clé d'idempotence. Les appels distants se font HORS transaction (la transaction ne couvre que
 * l'écriture en base, via {@link TransactionTemplate}), pour ne pas garder une connexion ouverte pendant un appel réseau.
 */
@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final ControleCommandeService controle;
    private final TransactionTemplate transaction;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository,
                                IdempotencyRecordRepository idempotencyRepository, ControleCommandeService controle,
                                PlatformTransactionManager transactionManager) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.controle = controle;
        this.transaction = new TransactionTemplate(transactionManager);
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

    /**
     * Création contrôlée et idempotente. Une nouvelle commande est toujours créée au statut BROUILLON, datée du jour.
     * <ol>
     *   <li>clé déjà connue : même contenu -> la commande d'origine est renvoyée (rejeu) ; contenu différent -> 409 ;</li>
     *   <li>sinon : fournisseur (local) puis produit/stock/seuil (OpenFeign) vérifiés ;</li>
     *   <li>commande + trace d'idempotence enregistrées dans la même transaction ; en cas de requêtes simultanées avec la
     *       même clé, la contrainte d'unicité fait échouer la seconde, qui renvoie alors la commande de la première.</li>
     * </ol>
     */
    public ResultatCreation creer(PurchaseOrder commande, String cleIdempotence) {
        String empreinte = empreinte(commande);

        var existant = idempotencyRepository.findByCle(cleIdempotence);
        if (existant.isPresent()) {
            return rejouer(existant.get(), empreinte);
        }

        verifierFournisseur(commande.getSupplierId());
        controle.verifierReapprovisionnement(commande.getProductId(), commande.getEntrepot());

        try {
            PurchaseOrder creee = transaction.execute(status -> {
                commande.setId(null);
                commande.setStatut(StatutCommande.BROUILLON);
                commande.setDate(LocalDate.now());
                PurchaseOrder sauvee = purchaseOrderRepository.save(commande);
                idempotencyRepository.saveAndFlush(new IdempotencyRecord(cleIdempotence, empreinte, sauvee.getId()));
                return sauvee;
            });
            return new ResultatCreation(creee, false);
        } catch (DataIntegrityViolationException e) {
            // Requête concurrente avec la même clé : la transaction (commande comprise) a été annulée.
            return idempotencyRepository.findByCle(cleIdempotence)
                    .map(r -> rejouer(r, empreinte))
                    .orElseThrow(() -> e);
        }
    }

    private ResultatCreation rejouer(IdempotencyRecord trace, String empreinte) {
        if (!trace.getEmpreinte().equals(empreinte)) {
            throw new ConflitException("La clé d'idempotence '" + trace.getCle()
                    + "' a déjà été utilisée avec un contenu différent");
        }
        return new ResultatCreation(trouver(trace.getPurchaseOrderId()), true);
    }

    /** Modification d'un brouillon : mêmes contrôles que la création (le brouillon peut changer de produit/entrepôt). */
    public PurchaseOrder modifier(Long id, PurchaseOrder nouvelle) {
        exigerStatut(trouver(id), StatutCommande.BROUILLON, "modifiée");
        verifierFournisseur(nouvelle.getSupplierId());
        controle.verifierReapprovisionnement(nouvelle.getProductId(), nouvelle.getEntrepot());
        return transaction.execute(status -> {
            PurchaseOrder commande = trouver(id);
            exigerStatut(commande, StatutCommande.BROUILLON, "modifiée");
            commande.setSupplierId(nouvelle.getSupplierId());
            commande.setProductId(nouvelle.getProductId());
            commande.setEntrepot(nouvelle.getEntrepot());
            commande.setQuantiteCommandee(nouvelle.getQuantiteCommandee());
            return purchaseOrderRepository.save(commande);
        });
    }

    /** La suppression d'un brouillon libère aussi sa clé d'idempotence. */
    @Transactional
    public void supprimer(Long id) {
        PurchaseOrder commande = trouver(id);
        exigerStatut(commande, StatutCommande.BROUILLON, "supprimée");
        idempotencyRepository.deleteByPurchaseOrderId(id);
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

    /** Empreinte SHA-256 du contenu métier de la requête (fournisseur, produit, entrepôt normalisé, quantité). */
    private static String empreinte(PurchaseOrder c) {
        String contenu = c.getSupplierId() + "|" + c.getProductId() + "|"
                + (c.getEntrepot() == null ? "" : c.getEntrepot().trim().toLowerCase()) + "|" + c.getQuantiteCommandee();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(contenu.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}

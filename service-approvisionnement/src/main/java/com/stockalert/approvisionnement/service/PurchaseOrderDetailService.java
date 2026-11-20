package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.dto.PurchaseOrderDetailDto;
import com.stockalert.approvisionnement.dto.SupplierDto;
import com.stockalert.approvisionnement.dto.client.AlerteDistanteDto;
import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.exception.RessourceDistanteIntrouvableException;
import com.stockalert.approvisionnement.exception.ServiceDistantException;
import com.stockalert.approvisionnement.mapper.SupplierMapper;
import com.stockalert.approvisionnement.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Construit la consultation enrichie d'une commande en interrogeant service-inventaire et service-alertes. */
@Service
public class PurchaseOrderDetailService {
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ServicesDistantsGateway distants;

    public PurchaseOrderDetailService(PurchaseOrderService purchaseOrderService, SupplierRepository supplierRepository,
                                      SupplierMapper supplierMapper, ServicesDistantsGateway distants) {
        this.purchaseOrderService = purchaseOrderService;
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.distants = distants;
    }

    public PurchaseOrderDetailDto consulter(Long id) {
        PurchaseOrder c = purchaseOrderService.trouver(id); // 404 si la commande n'existe pas
        List<String> avertissements = new ArrayList<>();

        SupplierDto fournisseur = supplierRepository.findById(c.getSupplierId())
                .map(supplierMapper::toDto).orElse(null);
        if (fournisseur == null) {
            avertissements.add("Fournisseur " + c.getSupplierId() + " introuvable localement");
        }

        ProduitDistantDto produit = null;
        try {
            produit = distants.produit(c.getProductId());
        } catch (RessourceDistanteIntrouvableException e) {
            avertissements.add("Produit " + c.getProductId() + " introuvable dans service-inventaire");
        } catch (ServiceDistantException e) {
            avertissements.add("Produit non enrichi : " + e.getMessage());
        }

        StockDistantDto stock = null;
        try {
            stock = distants.stocks(c.getProductId(), c.getEntrepot()).stream()
                    .filter(s -> s.entrepot() != null && s.entrepot().equalsIgnoreCase(c.getEntrepot()))
                    .findFirst().orElse(null);
            if (stock == null) {
                avertissements.add("Aucun niveau de stock pour le produit " + c.getProductId()
                        + " dans l'entrepôt '" + c.getEntrepot() + "'");
            }
        } catch (ServiceDistantException e) {
            avertissements.add("Stock non enrichi : " + e.getMessage());
        }

        AlerteDistanteDto alerte = null;
        try {
            alerte = distants.alertesOuvertes(c.getProductId()).stream()
                    .filter(a -> a.entrepot() != null && a.entrepot().equalsIgnoreCase(c.getEntrepot()))
                    .findFirst().orElse(null);
        } catch (ServiceDistantException e) {
            avertissements.add("Alerte non enrichie : " + e.getMessage());
        }

        return new PurchaseOrderDetailDto(c.getId(), c.getSupplierId(), c.getProductId(), c.getEntrepot(),
                c.getQuantiteCommandee(), c.getStatut(), c.getDate(), fournisseur, produit, stock, alerte,
                List.copyOf(avertissements));
    }
}

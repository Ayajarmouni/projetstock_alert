package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.entite.Supplier;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import com.stockalert.approvisionnement.repository.PurchaseOrderRepository;
import com.stockalert.approvisionnement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public SupplierService(SupplierRepository supplierRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<Supplier> lister() {
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Supplier trouver(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Fournisseur introuvable avec l'id " + id));
    }

    @Transactional
    public Supplier creer(Supplier supplier) {
        supplier.setId(null);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier modifier(Long id, Supplier nouveau) {
        Supplier supplier = trouver(id);
        supplier.setNom(nouveau.getNom());
        supplier.setEmail(nouveau.getEmail());
        supplier.setDelaiLivraisonJours(nouveau.getDelaiLivraisonJours());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void supprimer(Long id) {
        Supplier supplier = trouver(id);
        if (purchaseOrderRepository.existsBySupplierId(id)) {
            throw new ConflitException("Impossible de supprimer le fournisseur " + id + " : des commandes d'achat y sont rattachées");
        }
        supplierRepository.delete(supplier);
    }
}

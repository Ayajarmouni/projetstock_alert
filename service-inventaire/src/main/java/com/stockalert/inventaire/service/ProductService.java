package com.stockalert.inventaire.service;

import com.stockalert.inventaire.entite.Product;
import com.stockalert.inventaire.exception.ConflitException;
import com.stockalert.inventaire.exception.RessourceIntrouvableException;
import com.stockalert.inventaire.repository.ProductRepository;
import com.stockalert.inventaire.repository.StockLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;

    public ProductService(ProductRepository productRepository, StockLevelRepository stockLevelRepository) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> lister() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product trouver(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit introuvable avec l'id " + id));
    }

    @Transactional
    public Product creer(Product product) {
        if (productRepository.existsByReferenceIgnoreCase(product.getReference())) {
            throw new ConflitException("Un produit avec la référence '" + product.getReference() + "' existe déjà");
        }
        product.setId(null);
        return productRepository.save(product);
    }

    @Transactional
    public Product modifier(Long id, Product nouveau) {
        Product product = trouver(id);
        if (productRepository.existsByReferenceIgnoreCaseAndIdNot(nouveau.getReference(), id)) {
            throw new ConflitException("Un autre produit utilise déjà la référence '" + nouveau.getReference() + "'");
        }
        product.setNom(nouveau.getNom());
        product.setReference(nouveau.getReference());
        product.setCategorie(nouveau.getCategorie());
        return productRepository.save(product);
    }

    @Transactional
    public void supprimer(Long id) {
        Product product = trouver(id);
        if (stockLevelRepository.existsByProductId(id)) {
            throw new ConflitException("Impossible de supprimer le produit " + id + " : des niveaux de stock y sont rattachés");
        }
        productRepository.delete(product);
    }
}

package com.stockalert.inventaire.service;

import com.stockalert.inventaire.entite.StockLevel;
import com.stockalert.inventaire.exception.ConflitException;
import com.stockalert.inventaire.exception.RessourceIntrouvableException;
import com.stockalert.inventaire.repository.ProductRepository;
import com.stockalert.inventaire.repository.StockLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockLevelService {
    private static final Logger log = LoggerFactory.getLogger(StockLevelService.class);

    private final StockLevelRepository stockLevelRepository;
    private final ProductRepository productRepository;

    public StockLevelService(StockLevelRepository stockLevelRepository, ProductRepository productRepository) {
        this.stockLevelRepository = stockLevelRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<StockLevel> lister(Long productId, String entrepot) {
        List<StockLevel> niveaux = productId != null ? stockLevelRepository.findByProductId(productId)
                : stockLevelRepository.findAll();
        if (entrepot != null && !entrepot.isBlank()) {
            niveaux = niveaux.stream().filter(s -> s.getEntrepot().equalsIgnoreCase(entrepot)).toList();
        }
        return niveaux;
    }

    @Transactional(readOnly = true)
    public StockLevel trouver(Long id) {
        return stockLevelRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Niveau de stock introuvable avec l'id " + id));
    }

    @Transactional
    public StockLevel creer(StockLevel stock) {
        verifierProduit(stock.getProductId());
        if (stockLevelRepository.existsByProductIdAndEntrepotIgnoreCase(stock.getProductId(), stock.getEntrepot())) {
            throw new ConflitException("Un niveau de stock existe déjà pour le produit " + stock.getProductId()
                    + " dans l'entrepôt '" + stock.getEntrepot() + "'");
        }
        stock.setId(null);
        return stockLevelRepository.save(stock);
    }

    @Transactional
    public StockLevel modifier(Long id, StockLevel nouveau) {
        StockLevel stock = trouver(id);
        verifierProduit(nouveau.getProductId());
        if (stockLevelRepository.existsByProductIdAndEntrepotIgnoreCaseAndIdNot(
                nouveau.getProductId(), nouveau.getEntrepot(), id)) {
            throw new ConflitException("Un autre niveau de stock existe déjà pour le produit " + nouveau.getProductId()
                    + " dans l'entrepôt '" + nouveau.getEntrepot() + "'");
        }
        stock.setProductId(nouveau.getProductId());
        stock.setEntrepot(nouveau.getEntrepot());
        stock.setQuantite(nouveau.getQuantite());
        stock.setSeuilAlerte(nouveau.getSeuilAlerte());
        return stockLevelRepository.save(stock);
    }

    @Transactional
    public void supprimer(Long id) {
        stockLevelRepository.delete(trouver(id));
    }

    /** Sortie de stock : refusée (409) si la quantité disponible est insuffisante. */
    @Transactional
    public StockLevel sortie(Long id, int quantite) {
        StockLevel stock = verrouiller(id);
        if (stock.getQuantite() < quantite) {
            throw new ConflitException("Stock insuffisant : disponible " + stock.getQuantite() + ", demandé " + quantite);
        }
        stock.setQuantite(stock.getQuantite() - quantite);
        StockLevel sauvegarde = stockLevelRepository.save(stock);
        if (sauvegarde.getQuantite() < sauvegarde.getSeuilAlerte()) {
            // Jalon suivant : publication de l'événement Kafka "stock-low" vers service-alertes.
            log.warn("Stock sous le seuil d'alerte : produit {} / entrepôt {} ({} < {})",
                    sauvegarde.getProductId(), sauvegarde.getEntrepot(), sauvegarde.getQuantite(), sauvegarde.getSeuilAlerte());
        }
        return sauvegarde;
    }

    @Transactional
    public StockLevel entree(Long id, int quantite) {
        StockLevel stock = verrouiller(id);
        stock.setQuantite(stock.getQuantite() + quantite);
        return stockLevelRepository.save(stock);
    }

    private StockLevel verrouiller(Long id) {
        return stockLevelRepository.findPourModification(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Niveau de stock introuvable avec l'id " + id));
    }

    private void verifierProduit(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RessourceIntrouvableException("Produit introuvable avec l'id " + productId);
        }
    }
}

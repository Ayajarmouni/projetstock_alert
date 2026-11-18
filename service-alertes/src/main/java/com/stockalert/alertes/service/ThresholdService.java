package com.stockalert.alertes.service;

import com.stockalert.alertes.entite.Threshold;
import com.stockalert.alertes.exception.ConflitException;
import com.stockalert.alertes.exception.RessourceIntrouvableException;
import com.stockalert.alertes.repository.StockAlertRepository;
import com.stockalert.alertes.repository.ThresholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThresholdService {
    private final ThresholdRepository thresholdRepository;
    private final StockAlertRepository stockAlertRepository;

    public ThresholdService(ThresholdRepository thresholdRepository, StockAlertRepository stockAlertRepository) {
        this.thresholdRepository = thresholdRepository;
        this.stockAlertRepository = stockAlertRepository;
    }

    @Transactional(readOnly = true)
    public List<Threshold> lister() {
        return thresholdRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Threshold trouver(Long id) {
        return thresholdRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Règle de seuil introuvable avec l'id " + id));
    }

    @Transactional
    public Threshold creer(Threshold threshold) {
        if (thresholdRepository.existsByProductIdAndEntrepotIgnoreCase(threshold.getProductId(), threshold.getEntrepot())) {
            throw new ConflitException("Une règle de seuil existe déjà pour le produit " + threshold.getProductId()
                    + " dans l'entrepôt '" + threshold.getEntrepot() + "'");
        }
        threshold.setId(null);
        return thresholdRepository.save(threshold);
    }

    @Transactional
    public Threshold modifier(Long id, Threshold nouveau) {
        Threshold threshold = trouver(id);
        if (thresholdRepository.existsByProductIdAndEntrepotIgnoreCaseAndIdNot(
                nouveau.getProductId(), nouveau.getEntrepot(), id)) {
            throw new ConflitException("Une autre règle de seuil existe déjà pour le produit " + nouveau.getProductId()
                    + " dans l'entrepôt '" + nouveau.getEntrepot() + "'");
        }
        threshold.setProductId(nouveau.getProductId());
        threshold.setSupplierId(nouveau.getSupplierId());
        threshold.setEntrepot(nouveau.getEntrepot());
        threshold.setSeuilMin(nouveau.getSeuilMin());
        threshold.setQuantiteReappro(nouveau.getQuantiteReappro());
        threshold.setCanal(nouveau.getCanal());
        return thresholdRepository.save(threshold);
    }

    @Transactional
    public void supprimer(Long id) {
        Threshold threshold = trouver(id);
        if (stockAlertRepository.existsByThresholdId(id)) {
            throw new ConflitException("Impossible de supprimer la règle " + id + " : des alertes y sont rattachées");
        }
        thresholdRepository.delete(threshold);
    }
}

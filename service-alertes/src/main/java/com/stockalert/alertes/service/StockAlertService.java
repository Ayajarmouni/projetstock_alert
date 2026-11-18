package com.stockalert.alertes.service;

import com.stockalert.alertes.entite.StockAlert;
import com.stockalert.alertes.entite.Threshold;
import com.stockalert.alertes.enums.StatutAlerte;
import com.stockalert.alertes.exception.ConflitException;
import com.stockalert.alertes.exception.DonneesInvalidesException;
import com.stockalert.alertes.exception.RessourceIntrouvableException;
import com.stockalert.alertes.repository.StockAlertRepository;
import com.stockalert.alertes.repository.ThresholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockAlertService {
    private final StockAlertRepository stockAlertRepository;
    private final ThresholdRepository thresholdRepository;

    public StockAlertService(StockAlertRepository stockAlertRepository, ThresholdRepository thresholdRepository) {
        this.stockAlertRepository = stockAlertRepository;
        this.thresholdRepository = thresholdRepository;
    }

    @Transactional(readOnly = true)
    public List<StockAlert> lister(StatutAlerte statut, Long productId) {
        List<StockAlert> alertes = statut != null ? stockAlertRepository.findByStatut(statut)
                : stockAlertRepository.findAll();
        if (productId != null) {
            alertes = alertes.stream().filter(a -> productId.equals(a.getProductId())).toList();
        }
        return alertes;
    }

    @Transactional(readOnly = true)
    public StockAlert trouver(Long id) {
        return stockAlertRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Alerte introuvable avec l'id " + id));
    }

    /**
     * Crée une alerte OUVERTE pour une règle de seuil : le niveau détecté doit être sous le seuil minimum
     * et il ne peut pas y avoir deux alertes ouvertes pour la même règle.
     */
    @Transactional
    public StockAlert creer(Long thresholdId, int niveauDetecte) {
        Threshold regle = thresholdRepository.findById(thresholdId)
                .orElseThrow(() -> new RessourceIntrouvableException("Règle de seuil introuvable avec l'id " + thresholdId));
        if (niveauDetecte >= regle.getSeuilMin()) {
            throw new DonneesInvalidesException("Le niveau détecté (" + niveauDetecte
                    + ") doit être inférieur au seuil minimum (" + regle.getSeuilMin() + ")");
        }
        if (stockAlertRepository.existsByThresholdIdAndStatut(thresholdId, StatutAlerte.OUVERTE)) {
            throw new ConflitException("Une alerte ouverte existe déjà pour la règle de seuil " + thresholdId);
        }
        StockAlert alerte = new StockAlert();
        alerte.setThresholdId(regle.getId());
        alerte.setProductId(regle.getProductId());
        alerte.setEntrepot(regle.getEntrepot());
        alerte.setNiveauDetecte(niveauDetecte);
        alerte.setStatut(StatutAlerte.OUVERTE);
        return stockAlertRepository.save(alerte);
    }

    /** OUVERTE -> CLOTUREE (à terme déclenchée par l'événement Kafka "delivery-received"). */
    @Transactional
    public StockAlert cloturer(Long id) {
        StockAlert alerte = trouver(id);
        if (alerte.getStatut() != StatutAlerte.OUVERTE) {
            throw new ConflitException("L'alerte " + id + " est déjà clôturée");
        }
        alerte.setStatut(StatutAlerte.CLOTUREE);
        return stockAlertRepository.save(alerte);
    }

    @Transactional
    public void supprimer(Long id) {
        stockAlertRepository.delete(trouver(id));
    }
}

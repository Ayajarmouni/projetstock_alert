package com.stockalert.notification.service;

import com.stockalert.notification.dto.CommandeEvenement;
import com.stockalert.notification.dto.NotificationDto;
import com.stockalert.notification.entite.NotificationLog;
import com.stockalert.notification.exception.EvenementInvalideException;
import com.stockalert.notification.exception.RessourceIntrouvableException;
import com.stockalert.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Traitement d'un événement : validation, détection des doublons, construction et envoi de l'email, trace.
 * Livraison RabbitMQ « au moins une fois » : un même événement peut arriver deux fois (redémarrage, nouvel essai) ;
 * {@code eventId} permet de n'envoyer qu'un seul email.
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    public static final String STATUT_ENVOYEE = "ENVOYEE";

    private final EmailService emailService;
    private final NotificationLogRepository repository;

    public NotificationService(EmailService emailService, NotificationLogRepository repository) {
        this.emailService = emailService;
        this.repository = repository;
    }

    /**
     * @throws EvenementInvalideException événement inexploitable (-> DLQ sans nouvel essai)
     * @throws org.springframework.mail.MailException SMTP indisponible (-> nouvelles tentatives, puis DLQ)
     */
    public void traiter(CommandeEvenement e) {
        valider(e);
        if (repository.existsByEventId(e.eventId())) {
            log.info("Événement {} déjà traité : email non renvoyé", e.eventId());
            return;
        }
        boolean emise = CommandeEvenement.EMISE.equals(e.type());
        String sujet = emise ? "[Stock-Alert] Commande d'achat n°" + e.purchaseOrderId() + " émise"
                : "[Stock-Alert] Commande d'achat n°" + e.purchaseOrderId() + " livrée";

        emailService.envoyer(e.supplierEmail(), sujet, texte(e, emise), html(e, emise));
        repository.save(new NotificationLog(e.eventId(), e.type(), e.purchaseOrderId(), e.supplierEmail(), sujet,
                STATUT_ENVOYEE, LocalDateTime.now()));
        log.info("Email envoyé à {} : {}", e.supplierEmail(), sujet);
    }

    public List<NotificationDto> lister(Long purchaseOrderId) {
        List<NotificationLog> notifications = purchaseOrderId != null
                ? repository.findByPurchaseOrderIdOrderByIdAsc(purchaseOrderId) : repository.findAll();
        return notifications.stream().map(NotificationService::versDto).toList();
    }

    public NotificationDto trouver(Long id) {
        return repository.findById(id).map(NotificationService::versDto)
                .orElseThrow(() -> new RessourceIntrouvableException("Notification introuvable avec l'id " + id));
    }

    private static NotificationDto versDto(NotificationLog n) {
        return new NotificationDto(n.getId(), n.getEventId(), n.getType(), n.getPurchaseOrderId(), n.getDestinataire(),
                n.getSujet(), n.getStatut(), n.getDateEnvoi());
    }

    private static void valider(CommandeEvenement e) {
        if (e == null) {
            throw new EvenementInvalideException("Événement vide");
        }
        if (vide(e.eventId())) {
            throw new EvenementInvalideException("eventId manquant");
        }
        if (!CommandeEvenement.EMISE.equals(e.type()) && !CommandeEvenement.LIVREE.equals(e.type())) {
            throw new EvenementInvalideException("Type d'événement inconnu : " + e.type());
        }
        if (e.purchaseOrderId() == null) {
            throw new EvenementInvalideException("purchaseOrderId manquant");
        }
        if (vide(e.supplierEmail()) || !e.supplierEmail().contains("@")) {
            throw new EvenementInvalideException("Adresse email du fournisseur absente ou invalide : " + e.supplierEmail());
        }
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }

    // ---------- contenu des emails ----------

    private static String produit(CommandeEvenement e) {
        return vide(e.produitNom()) ? "Produit n°" + e.productId() : e.produitNom() + " (n°" + e.productId() + ")";
    }

    private static String texte(CommandeEvenement e, boolean emise) {
        String intro = emise
                ? "Une nouvelle commande d'achat vous est transmise. Merci de la préparer pour livraison."
                : "La livraison de la commande d'achat ci-dessous a bien été réceptionnée. Merci pour votre service.";
        return "Bonjour " + e.supplierNom() + ",\n\n" + intro + "\n\n"
                + "  Commande n°          : " + e.purchaseOrderId() + "\n"
                + "  Statut               : " + e.statut() + "\n"
                + "  Produit              : " + produit(e) + "\n"
                + "  Quantité             : " + e.quantiteCommandee() + "\n"
                + "  Entrepôt de livraison: " + e.entrepot() + "\n"
                + "  Date de commande     : " + e.date() + "\n"
                + (emise ? "  Délai convenu        : " + e.delaiLivraisonJours() + " jour(s)\n" : "")
                + "\nCordialement,\nL'équipe Stock-Alert\n";
    }

    private static String html(CommandeEvenement e, boolean emise) {
        String intro = emise
                ? "Une nouvelle commande d'achat vous est transmise. Merci de la préparer pour livraison."
                : "La livraison de la commande d'achat ci-dessous a bien été réceptionnée. Merci pour votre service.";
        return "<html><body style=\"font-family:Arial,sans-serif\">"
                + "<p>Bonjour " + h(e.supplierNom()) + ",</p><p>" + intro + "</p>"
                + "<table border=\"1\" cellpadding=\"6\" style=\"border-collapse:collapse\">"
                + ligne("Commande n°", String.valueOf(e.purchaseOrderId()))
                + ligne("Statut", e.statut())
                + ligne("Produit", produit(e))
                + ligne("Quantité", String.valueOf(e.quantiteCommandee()))
                + ligne("Entrepôt de livraison", e.entrepot())
                + ligne("Date de commande", e.date())
                + (emise ? ligne("Délai convenu", e.delaiLivraisonJours() + " jour(s)") : "")
                + "</table><p>Cordialement,<br/>L'équipe Stock-Alert</p></body></html>";
    }

    private static String ligne(String libelle, String valeur) {
        return "<tr><th align=\"left\">" + h(libelle) + "</th><td>" + h(valeur) + "</td></tr>";
    }

    private static String h(String s) {
        return HtmlUtils.htmlEscape(s == null ? "" : s);
    }
}

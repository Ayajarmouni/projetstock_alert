package com.stockalert.approvisionnement.messaging;

import com.stockalert.approvisionnement.service.ServicesDistantsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publie l'événement sur RabbitMQ UNIQUEMENT après la validation (commit) de la transaction qui a changé le statut de la
 * commande, et dans un thread séparé : la réponse HTTP n'attend ni RabbitMQ, ni service-notification, ni l'envoi du mail.
 * <ul>
 *   <li>transaction annulée (rollback) -> aucun événement, donc aucun email ;</li>
 *   <li>RabbitMQ indisponible -> l'erreur est journalisée, la commande reste valide (voir « limites » du README).</li>
 * </ul>
 */
@Component
public class CommandeEvenementPublisher {
    private static final Logger log = LoggerFactory.getLogger(CommandeEvenementPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ServicesDistantsGateway distants;

    public CommandeEvenementPublisher(RabbitTemplate rabbitTemplate, ServicesDistantsGateway distants) {
        this.rabbitTemplate = rabbitTemplate;
        this.distants = distants;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publier(CommandeEvenement evenement) {
        CommandeEvenement complet = enrichir(evenement);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, complet.routingKey(), complet, message -> {
                message.getMessageProperties().setMessageId(complet.eventId());
                return message;
            });
            log.info("Événement {} publié (commande {}, clé {}, eventId {})", complet.type(), complet.purchaseOrderId(),
                    complet.routingKey(), complet.eventId());
        } catch (AmqpException e) {
            log.error("Publication RabbitMQ impossible pour la commande {} ({}) : {}", complet.purchaseOrderId(),
                    complet.type(), e.getMessage());
        }
    }

    /** Nom du produit pour un email lisible. Facultatif : en cas d'échec, l'événement part sans nom de produit. */
    private CommandeEvenement enrichir(CommandeEvenement evenement) {
        try {
            return evenement.avecProduitNom(distants.produit(evenement.productId()).nom());
        } catch (RuntimeException e) {
            log.warn("Nom du produit {} indisponible, événement publié sans enrichissement : {}", evenement.productId(),
                    e.getMessage());
            return evenement;
        }
    }
}

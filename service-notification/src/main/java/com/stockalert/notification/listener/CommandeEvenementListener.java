package com.stockalert.notification.listener;

import com.stockalert.notification.config.RabbitConfig;
import com.stockalert.notification.dto.CommandeEvenement;
import com.stockalert.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consommateur de la file {@code notification.purchase-order}. Toute exception remonte au conteneur Spring AMQP :
 * nouvelles tentatives (configurées dans config-repo), puis rejet vers la DLQ. Le message n'est acquitté (ack) qu'après
 * l'envoi réussi de l'email : s'il échoue ou si le service s'arrête en cours de traitement, RabbitMQ le conserve.
 */
@Component
public class CommandeEvenementListener {
    private static final Logger log = LoggerFactory.getLogger(CommandeEvenementListener.class);

    private final NotificationService notifications;

    public CommandeEvenementListener(NotificationService notifications) {
        this.notifications = notifications;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void recevoir(CommandeEvenement evenement) {
        log.info("Événement reçu : {} commande {} (eventId {})", evenement.type(), evenement.purchaseOrderId(),
                evenement.eventId());
        notifications.traiter(evenement);
    }
}

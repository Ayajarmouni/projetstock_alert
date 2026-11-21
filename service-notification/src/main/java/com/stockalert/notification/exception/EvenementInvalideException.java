package com.stockalert.notification.exception;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Message reçu mais inexploitable (champ obligatoire manquant, type inconnu...). Un nouvel essai ne changerait rien :
 * le message est rejeté sans remise en file et part dans la file des messages morts (DLQ).
 */
public class EvenementInvalideException extends AmqpRejectAndDontRequeueException {
    public EvenementInvalideException(String message) {
        super(message);
    }
}

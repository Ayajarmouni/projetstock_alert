package com.stockalert.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/** Envoi SMTP (Mailpit en développement : localhost:1025). Une panne SMTP lève {@link MailSendException}. */
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String expediteur;

    public EmailService(JavaMailSender mailSender,
                        @Value("${notification.expediteur:stock-alert@stockalert.local}") String expediteur) {
        this.mailSender = mailSender;
        this.expediteur = expediteur;
    }

    public void envoyer(String destinataire, String sujet, String texte, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(expediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(texte, html);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Construction de l'email impossible : " + e.getMessage(), e);
        }
    }
}

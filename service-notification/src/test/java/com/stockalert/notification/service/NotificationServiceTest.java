package com.stockalert.notification.service;

import com.stockalert.notification.dto.CommandeEvenement;
import com.stockalert.notification.entite.NotificationLog;
import com.stockalert.notification.exception.EvenementInvalideException;
import com.stockalert.notification.repository.NotificationLogRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {
    private JavaMailSender mailSender;
    private NotificationLogRepository repository;
    private NotificationService service;

    @BeforeEach
    void init() {
        mailSender = Mockito.mock(JavaMailSender.class);
        repository = Mockito.mock(NotificationLogRepository.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));
        service = new NotificationService(new EmailService(mailSender, "stock-alert@test.local"), repository);
    }

    private CommandeEvenement evenement(String type, String email) {
        return new CommandeEvenement("evt-1", type, "2026-11-21T10:00:00Z", 12L,
                CommandeEvenement.EMISE.equals(type) ? "EMISE" : "LIVREE", "2026-11-21", 1L, "TechDistrib Maroc", email, 3,
                1L, "Dell Latitude 5440", "Rabat", 30);
    }

    @Test
    void evenementEmiseEnvoieUnEmailEtEnregistreUneTrace() {
        when(repository.existsByEventId("evt-1")).thenReturn(false);

        service.traiter(evenement(CommandeEvenement.EMISE, "commandes@techdistrib.ma"));

        verify(mailSender).send(any(MimeMessage.class));
        ArgumentCaptor<NotificationLog> trace = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repository).save(trace.capture());
        assertThat(trace.getValue().getDestinataire()).isEqualTo("commandes@techdistrib.ma");
        assertThat(trace.getValue().getSujet()).contains("n°12").contains("émise");
        assertThat(trace.getValue().getStatut()).isEqualTo("ENVOYEE");
    }

    @Test
    void evenementLivreeAUnSujetDeLivraison() {
        service.traiter(evenement(CommandeEvenement.LIVREE, "commandes@techdistrib.ma"));

        ArgumentCaptor<NotificationLog> trace = ArgumentCaptor.forClass(NotificationLog.class);
        verify(repository).save(trace.capture());
        assertThat(trace.getValue().getSujet()).contains("livrée");
    }

    @Test
    void evenementDejaTraiteNEnvoieRien() {
        when(repository.existsByEventId("evt-1")).thenReturn(true);

        service.traiter(evenement(CommandeEvenement.EMISE, "commandes@techdistrib.ma"));

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(repository, never()).save(any(NotificationLog.class));
    }

    @Test
    void emailAbsentOuInvalideEstRejeteSansEnvoi() {
        assertThatThrownBy(() -> service.traiter(evenement(CommandeEvenement.EMISE, null)))
                .isInstanceOf(EvenementInvalideException.class);
        assertThatThrownBy(() -> service.traiter(evenement(CommandeEvenement.EMISE, "pas-un-email")))
                .isInstanceOf(EvenementInvalideException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void typeInconnuEstRejete() {
        assertThatThrownBy(() -> service.traiter(evenement("AUTRE", "a@b.ma")))
                .isInstanceOf(EvenementInvalideException.class)
                .hasMessageContaining("AUTRE");
    }

    @Test
    void panneSmtpRemonteLErreurEtNEnregistreAucuneTrace() {
        doThrow(new MailSendException("Connection refused")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.traiter(evenement(CommandeEvenement.EMISE, "commandes@techdistrib.ma")))
                .isInstanceOf(MailSendException.class);

        verify(repository, never()).save(any(NotificationLog.class));
    }
}

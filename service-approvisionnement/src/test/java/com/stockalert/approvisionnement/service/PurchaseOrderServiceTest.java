package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.entite.IdempotencyRecord;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.entite.Supplier;
import com.stockalert.approvisionnement.messaging.CommandeEvenement;
import com.stockalert.approvisionnement.enums.StatutCommande;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import com.stockalert.approvisionnement.exception.ServiceDistantIndisponibleException;
import com.stockalert.approvisionnement.repository.IdempotencyRecordRepository;
import com.stockalert.approvisionnement.repository.PurchaseOrderRepository;
import com.stockalert.approvisionnement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseOrderServiceTest {
    private PurchaseOrderRepository commandes;
    private SupplierRepository fournisseurs;
    private IdempotencyRecordRepository traces;
    private ControleCommandeService controle;
    private PurchaseOrderService service;
    private ApplicationEventPublisher evenements;

    @BeforeEach
    void init() {
        commandes = Mockito.mock(PurchaseOrderRepository.class);
        fournisseurs = Mockito.mock(SupplierRepository.class);
        traces = Mockito.mock(IdempotencyRecordRepository.class);
        controle = Mockito.mock(ControleCommandeService.class);
        evenements = Mockito.mock(ApplicationEventPublisher.class);
        service = new PurchaseOrderService(commandes, fournisseurs, traces, controle,
                Mockito.mock(PlatformTransactionManager.class), evenements);
        when(fournisseurs.existsById(1L)).thenReturn(true);
        when(commandes.save(any(PurchaseOrder.class))).thenAnswer(inv -> {
            PurchaseOrder c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });
    }

    private PurchaseOrder demande(int quantite) {
        PurchaseOrder c = new PurchaseOrder();
        c.setSupplierId(1L);
        c.setProductId(1L);
        c.setEntrepot("Rabat");
        c.setQuantiteCommandee(quantite);
        return c;
    }

    @Test
    void premiereCreationEnregistreCommandeEtTrace() {
        when(traces.findByCle("K1")).thenReturn(Optional.empty());

        ResultatCreation r = service.creer(demande(30), "K1");

        assertThat(r.rejouee()).isFalse();
        assertThat(r.commande().getId()).isEqualTo(42L);
        assertThat(r.commande().getStatut()).isEqualTo(StatutCommande.BROUILLON);
        verify(controle).verifierReapprovisionnement(1L, "Rabat");
        verify(traces).saveAndFlush(any(IdempotencyRecord.class));
    }

    @Test
    void memeCleMemeContenuRejoueSansCreerNiControler() {
        when(traces.findByCle("K1")).thenReturn(Optional.empty());
        service.creer(demande(30), "K1");
        ArgumentCaptor<IdempotencyRecord> trace = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(traces).saveAndFlush(trace.capture());

        when(traces.findByCle("K1")).thenReturn(Optional.of(trace.getValue()));
        PurchaseOrder existante = demande(30);
        existante.setId(42L);
        when(commandes.findById(42L)).thenReturn(Optional.of(existante));

        ResultatCreation r = service.creer(demande(30), "K1");

        assertThat(r.rejouee()).isTrue();
        assertThat(r.commande().getId()).isEqualTo(42L);
        verify(commandes, times(1)).save(any(PurchaseOrder.class));
        verify(controle, times(1)).verifierReapprovisionnement(1L, "Rabat");
    }

    @Test
    void memeCleContenuDifferentDonneConflit() {
        when(traces.findByCle("K1")).thenReturn(Optional.empty());
        service.creer(demande(30), "K1");
        ArgumentCaptor<IdempotencyRecord> trace = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(traces).saveAndFlush(trace.capture());
        when(traces.findByCle("K1")).thenReturn(Optional.of(trace.getValue()));

        assertThatThrownBy(() -> service.creer(demande(99), "K1"))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("contenu différent");
    }

    @Test
    void requeteConcurrenteAvecLaMemeCleRenvoieLaCommandeGagnante() {
        // 1re creation (la "gagnante") pour obtenir une trace coherente
        when(traces.findByCle("K1")).thenReturn(Optional.empty());
        service.creer(demande(30), "K1");
        ArgumentCaptor<IdempotencyRecord> trace = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(traces).saveAndFlush(trace.capture());
        IdempotencyRecord gagnante = trace.getValue();

        // 2e requete : ne voit rien au depart, echoue sur la contrainte d'unicite, puis relit la trace
        when(traces.findByCle("K1")).thenReturn(Optional.empty(), Optional.of(gagnante));
        when(traces.saveAndFlush(any(IdempotencyRecord.class))).thenThrow(new DataIntegrityViolationException("uk_idempotency_cle"));
        PurchaseOrder existante = demande(30);
        existante.setId(42L);
        when(commandes.findById(42L)).thenReturn(Optional.of(existante));

        ResultatCreation r = service.creer(demande(30), "K1");

        assertThat(r.rejouee()).isTrue();
        assertThat(r.commande().getId()).isEqualTo(42L);
    }

    @Test
    void fournisseurInconnuDonne404SansAppelDistant() {
        when(traces.findByCle("K2")).thenReturn(Optional.empty());
        PurchaseOrder c = demande(5);
        c.setSupplierId(9999L);

        assertThatThrownBy(() -> service.creer(c, "K2")).isInstanceOf(RessourceIntrouvableException.class);
        verify(controle, never()).verifierReapprovisionnement(any(), any());
    }

    @Test
    void erreurDeCommunicationNEnregistreRienEtLaCleResteLibre() {
        when(traces.findByCle("K3")).thenReturn(Optional.empty());
        Mockito.doThrow(new ServiceDistantIndisponibleException("service-inventaire", "injoignable", null))
                .when(controle).verifierReapprovisionnement(1L, "Rabat");

        assertThatThrownBy(() -> service.creer(demande(30), "K3")).isInstanceOf(ServiceDistantIndisponibleException.class);
        verify(commandes, never()).save(any(PurchaseOrder.class));
        verify(traces, never()).saveAndFlush(any(IdempotencyRecord.class));
    }

    // ---------- V5 : événements de notification ----------

    private PurchaseOrder commandeExistante(StatutCommande statut) {
        PurchaseOrder c = demande(30);
        c.setId(42L);
        c.setStatut(statut);
        c.setDate(java.time.LocalDate.of(2026, 11, 21));
        when(commandes.findById(42L)).thenReturn(Optional.of(c));
        Supplier f = new Supplier();
        f.setId(1L);
        f.setNom("TechDistrib Maroc");
        f.setEmail("commandes@techdistrib.ma");
        f.setDelaiLivraisonJours(3);
        when(fournisseurs.findById(1L)).thenReturn(Optional.of(f));
        return c;
    }

    @Test
    void emettreAnnonceUnEvenementEmiseAvecLeFournisseur() {
        commandeExistante(StatutCommande.BROUILLON);

        service.emettre(42L);

        ArgumentCaptor<CommandeEvenement> evt = ArgumentCaptor.forClass(CommandeEvenement.class);
        verify(evenements).publishEvent(evt.capture());
        assertThat(evt.getValue().type()).isEqualTo(CommandeEvenement.EMISE);
        assertThat(evt.getValue().routingKey()).isEqualTo("purchase-order.emise");
        assertThat(evt.getValue().purchaseOrderId()).isEqualTo(42L);
        assertThat(evt.getValue().supplierEmail()).isEqualTo("commandes@techdistrib.ma");
        assertThat(evt.getValue().statut()).isEqualTo("EMISE");
        assertThat(evt.getValue().eventId()).isNotBlank();
    }

    @Test
    void livrerAnnonceUnEvenementLivree() {
        commandeExistante(StatutCommande.EMISE);

        service.livrer(42L);

        ArgumentCaptor<CommandeEvenement> evt = ArgumentCaptor.forClass(CommandeEvenement.class);
        verify(evenements).publishEvent(evt.capture());
        assertThat(evt.getValue().routingKey()).isEqualTo("purchase-order.livree");
    }

    @Test
    void transitionRefuseeNAnnonceAucunEvenement() {
        commandeExistante(StatutCommande.EMISE);

        assertThatThrownBy(() -> service.emettre(42L)).isInstanceOf(ConflitException.class);

        verify(evenements, never()).publishEvent(any(Object.class));
    }
}

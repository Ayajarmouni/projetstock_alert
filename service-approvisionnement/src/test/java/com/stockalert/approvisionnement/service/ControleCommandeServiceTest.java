package com.stockalert.approvisionnement.service;

import com.stockalert.approvisionnement.dto.client.ProduitDistantDto;
import com.stockalert.approvisionnement.dto.client.StockDistantDto;
import com.stockalert.approvisionnement.exception.ConflitException;
import com.stockalert.approvisionnement.exception.RessourceDistanteIntrouvableException;
import com.stockalert.approvisionnement.exception.RessourceIntrouvableException;
import com.stockalert.approvisionnement.exception.ServiceDistantIndisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ControleCommandeServiceTest {
    private ServicesDistantsGateway distants;
    private ControleCommandeService controle;

    @BeforeEach
    void init() {
        distants = Mockito.mock(ServicesDistantsGateway.class);
        controle = new ControleCommandeService(distants);
        when(distants.produit(anyLong())).thenReturn(new ProduitDistantDto(1L, "Dell", "DELL", "Informatique"));
    }

    @Test
    void stockBasAutoriseLeReapprovisionnement() {
        when(distants.stocks(1L, "Rabat")).thenReturn(List.of(new StockDistantDto(2L, 1L, "Rabat", 8, 10)));
        assertThat(controle.verifierReapprovisionnement(1L, "Rabat").quantite()).isEqualTo(8);
    }

    @Test
    void quantiteEgaleAuSeuilEstAutorisee() {
        when(distants.stocks(1L, "Rabat")).thenReturn(List.of(new StockDistantDto(2L, 1L, "rabat", 10, 10)));
        assertThat(controle.verifierReapprovisionnement(1L, "Rabat")).isNotNull();
    }

    @Test
    void produitInconnuDonne404() {
        when(distants.produit(9999L)).thenThrow(new RessourceDistanteIntrouvableException("service-inventaire", "absent"));
        assertThatThrownBy(() -> controle.verifierReapprovisionnement(9999L, "Rabat"))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("Produit introuvable");
    }

    @Test
    void stockAbsentDansLEntrepotDonne404() {
        when(distants.stocks(1L, "Agadir")).thenReturn(List.of());
        assertThatThrownBy(() -> controle.verifierReapprovisionnement(1L, "Agadir"))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("Agadir");
    }

    @Test
    void stockSuffisantEstRefuse() {
        when(distants.stocks(1L, "Casablanca")).thenReturn(List.of(new StockDistantDto(1L, 1L, "Casablanca", 25, 10)));
        assertThatThrownBy(() -> controle.verifierReapprovisionnement(1L, "Casablanca"))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("suffisant");
    }

    @Test
    void serviceIndisponibleEstPropage() {
        when(distants.stocks(anyLong(), anyString()))
                .thenThrow(new ServiceDistantIndisponibleException("service-inventaire", "injoignable", null));
        assertThatThrownBy(() -> controle.verifierReapprovisionnement(1L, "Rabat"))
                .isInstanceOf(ServiceDistantIndisponibleException.class);
    }
}

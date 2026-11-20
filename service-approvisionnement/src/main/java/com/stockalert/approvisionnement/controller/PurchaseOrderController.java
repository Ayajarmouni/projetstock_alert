package com.stockalert.approvisionnement.controller;

import com.stockalert.approvisionnement.dto.PurchaseOrderDetailDto;
import com.stockalert.approvisionnement.dto.PurchaseOrderDto;
import com.stockalert.approvisionnement.dto.PurchaseOrderRequestDto;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.enums.StatutCommande;
import com.stockalert.approvisionnement.exception.DonneesInvalidesException;
import com.stockalert.approvisionnement.mapper.PurchaseOrderMapper;
import com.stockalert.approvisionnement.service.PurchaseOrderDetailService;
import com.stockalert.approvisionnement.service.PurchaseOrderService;
import com.stockalert.approvisionnement.service.ResultatCreation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderDetailService purchaseOrderDetailService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                   PurchaseOrderDetailService purchaseOrderDetailService,
                                   PurchaseOrderMapper purchaseOrderMapper) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseOrderDetailService = purchaseOrderDetailService;
        this.purchaseOrderMapper = purchaseOrderMapper;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDto>> lister(
            @RequestParam(name = "statut", required = false) StatutCommande statut,
            @RequestParam(name = "supplierId", required = false) Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.lister(statut, supplierId).stream()
                .map(purchaseOrderMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDetailDto> trouver(@PathVariable("id") Long id) {
        // Consultation enrichie (V4) : fournisseur, produit, stock et alerte ouverte
        return ResponseEntity.ok(purchaseOrderDetailService.consulter(id));
    }

    /**
     * Création contrôlée et idempotente. L'en-tête {@code Idempotency-Key} est obligatoire.
     * 201 + Location : commande créée ; 200 + {@code Idempotent-Replayed: true} : clé déjà utilisée avec le même contenu
     * (la commande d'origine est renvoyée, rien n'est créé).
     */
    @PostMapping
    public ResponseEntity<PurchaseOrderDto> creer(
            @RequestHeader(name = "Idempotency-Key", required = false) String cleIdempotence,
            @Valid @RequestBody PurchaseOrderRequestDto dto) {
        if (cleIdempotence == null || cleIdempotence.isBlank()) {
            throw new DonneesInvalidesException("L'en-tête Idempotency-Key est obligatoire pour créer une commande");
        }
        if (cleIdempotence.length() > 100) {
            throw new DonneesInvalidesException("L'en-tête Idempotency-Key ne doit pas dépasser 100 caractères");
        }
        ResultatCreation resultat = purchaseOrderService.creer(purchaseOrderMapper.toEntity(dto), cleIdempotence.trim());
        PurchaseOrderDto corps = purchaseOrderMapper.toDto(resultat.commande());
        if (resultat.rejouee()) {
            return ResponseEntity.ok().header("Idempotent-Replayed", "true").body(corps);
        }
        return ResponseEntity.created(URI.create("/purchase-orders/" + corps.id())).body(corps);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderDto> modifier(@PathVariable("id") Long id,
                                                     @Valid @RequestBody PurchaseOrderRequestDto dto) {
        return ResponseEntity.ok(purchaseOrderMapper.toDto(
                purchaseOrderService.modifier(id, purchaseOrderMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        purchaseOrderService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/emettre")
    public ResponseEntity<PurchaseOrderDto> emettre(@PathVariable("id") Long id) {
        return ResponseEntity.ok(purchaseOrderMapper.toDto(purchaseOrderService.emettre(id)));
    }

    @PostMapping("/{id}/livrer")
    public ResponseEntity<PurchaseOrderDto> livrer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(purchaseOrderMapper.toDto(purchaseOrderService.livrer(id)));
    }
}

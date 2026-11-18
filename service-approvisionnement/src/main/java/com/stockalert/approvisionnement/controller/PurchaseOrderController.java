package com.stockalert.approvisionnement.controller;

import com.stockalert.approvisionnement.dto.PurchaseOrderDto;
import com.stockalert.approvisionnement.dto.PurchaseOrderRequestDto;
import com.stockalert.approvisionnement.entite.PurchaseOrder;
import com.stockalert.approvisionnement.enums.StatutCommande;
import com.stockalert.approvisionnement.mapper.PurchaseOrderMapper;
import com.stockalert.approvisionnement.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService, PurchaseOrderMapper purchaseOrderMapper) {
        this.purchaseOrderService = purchaseOrderService;
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
    public ResponseEntity<PurchaseOrderDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(purchaseOrderMapper.toDto(purchaseOrderService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDto> creer(@Valid @RequestBody PurchaseOrderRequestDto dto) {
        PurchaseOrder creee = purchaseOrderService.creer(purchaseOrderMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/purchase-orders/" + creee.getId())).body(purchaseOrderMapper.toDto(creee));
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

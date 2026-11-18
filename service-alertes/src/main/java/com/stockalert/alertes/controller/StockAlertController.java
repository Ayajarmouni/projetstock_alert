package com.stockalert.alertes.controller;

import com.stockalert.alertes.dto.StockAlertDto;
import com.stockalert.alertes.dto.StockAlertRequestDto;
import com.stockalert.alertes.entite.StockAlert;
import com.stockalert.alertes.enums.StatutAlerte;
import com.stockalert.alertes.mapper.StockAlertMapper;
import com.stockalert.alertes.service.StockAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/stock-alerts")
public class StockAlertController {
    private final StockAlertService stockAlertService;
    private final StockAlertMapper stockAlertMapper;

    public StockAlertController(StockAlertService stockAlertService, StockAlertMapper stockAlertMapper) {
        this.stockAlertService = stockAlertService;
        this.stockAlertMapper = stockAlertMapper;
    }

    @GetMapping
    public ResponseEntity<List<StockAlertDto>> lister(
            @RequestParam(name = "statut", required = false) StatutAlerte statut,
            @RequestParam(name = "productId", required = false) Long productId) {
        return ResponseEntity.ok(stockAlertService.lister(statut, productId).stream()
                .map(stockAlertMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockAlertDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(stockAlertMapper.toDto(stockAlertService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<StockAlertDto> creer(@Valid @RequestBody StockAlertRequestDto dto) {
        StockAlert creee = stockAlertService.creer(dto.thresholdId(), dto.niveauDetecte());
        return ResponseEntity.created(URI.create("/stock-alerts/" + creee.getId())).body(stockAlertMapper.toDto(creee));
    }

    @PostMapping("/{id}/cloturer")
    public ResponseEntity<StockAlertDto> cloturer(@PathVariable("id") Long id) {
        return ResponseEntity.ok(stockAlertMapper.toDto(stockAlertService.cloturer(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        stockAlertService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

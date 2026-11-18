package com.stockalert.inventaire.controller;

import com.stockalert.inventaire.dto.MouvementStockDto;
import com.stockalert.inventaire.dto.StockLevelDto;
import com.stockalert.inventaire.entite.StockLevel;
import com.stockalert.inventaire.mapper.StockLevelMapper;
import com.stockalert.inventaire.service.StockLevelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/stock-levels")
public class StockLevelController {
    private final StockLevelService stockLevelService;
    private final StockLevelMapper stockLevelMapper;

    public StockLevelController(StockLevelService stockLevelService, StockLevelMapper stockLevelMapper) {
        this.stockLevelService = stockLevelService;
        this.stockLevelMapper = stockLevelMapper;
    }

    @GetMapping
    public ResponseEntity<List<StockLevelDto>> lister(
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "entrepot", required = false) String entrepot) {
        return ResponseEntity.ok(stockLevelService.lister(productId, entrepot).stream()
                .map(stockLevelMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockLevelDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(stockLevelMapper.toDto(stockLevelService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<StockLevelDto> creer(@Valid @RequestBody StockLevelDto dto) {
        StockLevel cree = stockLevelService.creer(stockLevelMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/stock-levels/" + cree.getId())).body(stockLevelMapper.toDto(cree));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockLevelDto> modifier(@PathVariable("id") Long id, @Valid @RequestBody StockLevelDto dto) {
        return ResponseEntity.ok(stockLevelMapper.toDto(stockLevelService.modifier(id, stockLevelMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        stockLevelService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sortie")
    public ResponseEntity<StockLevelDto> sortie(@PathVariable("id") Long id, @Valid @RequestBody MouvementStockDto mouvement) {
        return ResponseEntity.ok(stockLevelMapper.toDto(stockLevelService.sortie(id, mouvement.quantite())));
    }

    @PostMapping("/{id}/entree")
    public ResponseEntity<StockLevelDto> entree(@PathVariable("id") Long id, @Valid @RequestBody MouvementStockDto mouvement) {
        return ResponseEntity.ok(stockLevelMapper.toDto(stockLevelService.entree(id, mouvement.quantite())));
    }
}

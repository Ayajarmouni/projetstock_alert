package com.stockalert.approvisionnement.controller;

import com.stockalert.approvisionnement.dto.SupplierDto;
import com.stockalert.approvisionnement.entite.Supplier;
import com.stockalert.approvisionnement.mapper.SupplierMapper;
import com.stockalert.approvisionnement.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    public SupplierController(SupplierService supplierService, SupplierMapper supplierMapper) {
        this.supplierService = supplierService;
        this.supplierMapper = supplierMapper;
    }

    @GetMapping
    public ResponseEntity<List<SupplierDto>> lister() {
        return ResponseEntity.ok(supplierService.lister().stream().map(supplierMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supplierMapper.toDto(supplierService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<SupplierDto> creer(@Valid @RequestBody SupplierDto dto) {
        Supplier cree = supplierService.creer(supplierMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/suppliers/" + cree.getId())).body(supplierMapper.toDto(cree));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> modifier(@PathVariable("id") Long id, @Valid @RequestBody SupplierDto dto) {
        return ResponseEntity.ok(supplierMapper.toDto(supplierService.modifier(id, supplierMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        supplierService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

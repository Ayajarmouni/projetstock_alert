package com.stockalert.inventaire.controller;

import com.stockalert.inventaire.dto.ProductDto;
import com.stockalert.inventaire.entite.Product;
import com.stockalert.inventaire.mapper.ProductMapper;
import com.stockalert.inventaire.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> lister() {
        return ResponseEntity.ok(productService.lister().stream().map(productMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productMapper.toDto(productService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<ProductDto> creer(@Valid @RequestBody ProductDto dto) {
        Product cree = productService.creer(productMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/products/" + cree.getId())).body(productMapper.toDto(cree));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> modifier(@PathVariable("id") Long id, @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productMapper.toDto(productService.modifier(id, productMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        productService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

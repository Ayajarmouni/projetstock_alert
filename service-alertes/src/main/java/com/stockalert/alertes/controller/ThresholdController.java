package com.stockalert.alertes.controller;

import com.stockalert.alertes.dto.ThresholdDto;
import com.stockalert.alertes.entite.Threshold;
import com.stockalert.alertes.mapper.ThresholdMapper;
import com.stockalert.alertes.service.ThresholdService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/thresholds")
public class ThresholdController {
    private final ThresholdService thresholdService;
    private final ThresholdMapper thresholdMapper;

    public ThresholdController(ThresholdService thresholdService, ThresholdMapper thresholdMapper) {
        this.thresholdService = thresholdService;
        this.thresholdMapper = thresholdMapper;
    }

    @GetMapping
    public ResponseEntity<List<ThresholdDto>> lister() {
        return ResponseEntity.ok(thresholdService.lister().stream().map(thresholdMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThresholdDto> trouver(@PathVariable("id") Long id) {
        return ResponseEntity.ok(thresholdMapper.toDto(thresholdService.trouver(id)));
    }

    @PostMapping
    public ResponseEntity<ThresholdDto> creer(@Valid @RequestBody ThresholdDto dto) {
        Threshold cree = thresholdService.creer(thresholdMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/thresholds/" + cree.getId())).body(thresholdMapper.toDto(cree));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThresholdDto> modifier(@PathVariable("id") Long id, @Valid @RequestBody ThresholdDto dto) {
        return ResponseEntity.ok(thresholdMapper.toDto(thresholdService.modifier(id, thresholdMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable("id") Long id) {
        thresholdService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

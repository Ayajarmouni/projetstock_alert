package com.stockalert.inventaire.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GestionnaireExceptions {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurDto> ressourceIntrouvable(RessourceIntrouvableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ErreurDto> conflit(ConflitException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(DonneesInvalidesException.class)
    public ResponseEntity<ErreurDto> donneesInvalides(DonneesInvalidesException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .sorted()
                .toList();
        return reponse(HttpStatus.BAD_REQUEST, "Données de la requête invalides", req, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurDto> corpsIllisible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST,
                "Corps de la requête illisible : JSON mal formé ou valeur d'un type/énumération incorrect", req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurDto> typeIncorrect(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = "Paramètre '" + ex.getName() + "' invalide : valeur '" + ex.getValue() + "'";
        return reponse(HttpStatus.BAD_REQUEST, message, req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurDto> integrite(DataIntegrityViolationException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, "Violation d'une contrainte d'intégrité des données", req, List.of());
    }

    private ResponseEntity<ErreurDto> reponse(HttpStatus status, String message, HttpServletRequest req, List<String> details) {
        ErreurDto corps = new ErreurDto(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequestURI(), details);
        return ResponseEntity.status(status).body(corps);
    }
}

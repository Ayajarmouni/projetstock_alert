package com.stockalert.notification.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GestionnaireExceptions {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurDto> introuvable(RessourceIntrouvableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurDto> typeIncorrect(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, "Paramètre '" + ex.getName() + "' invalide : " + ex.getValue(), req);
    }

    private ResponseEntity<ErreurDto> reponse(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErreurDto(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI(), List.of()));
    }
}

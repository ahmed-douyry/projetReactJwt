package org.example.backend.web.advice;

import org.example.backend.dto.response.ErrorResponse;
import org.example.backend.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<ErrorResponse> error(int s, String m) {
        return ResponseEntity.status(s).body(new ErrorResponse(s, m, Map.of()));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> application(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException e) {
        return error(403, "Accès interdit");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Données invalides", errors));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ErrorResponse> malformed(Exception e) {
        return error(400, "Requête ou valeur invalide");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(Exception e) {
        return error(409, "Une donnée existe déjà ou ne respecte pas les contraintes");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ErrorResponse> concurrent(Exception e) {
        return error(409, "Cette donnée a été modifiée. Rechargez la page.");
    }
}

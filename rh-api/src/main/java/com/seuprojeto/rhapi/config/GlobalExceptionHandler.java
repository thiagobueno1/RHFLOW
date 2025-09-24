package com.seuprojeto.rhapi.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> onMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
                ));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "validation",
                "fields", fields
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> onConstraintViolation(ConstraintViolationException ex) {
        var msgs = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath()+": "+v.getMessage())
                .toList();
        return ResponseEntity.badRequest().body(Map.of(
                "error", "validation",
                "messages", msgs
        ));
    }
}

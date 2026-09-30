package com.notebook.shareApp.controller;

import com.notebook.shareApp.util.ApiException;
import com.notebook.shareApp.util.RegistrationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One error shape for the JS: {"message": "...", "errors": {"fieldName": "message"}}.
 * auth.js maps "errors" keys onto the register form fields.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("message", "Please fix the highlighted fields", "errors", errors));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, Object>> registration(RegistrationException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage(), "errors", Map.of(ex.getField(), ex.getMessage())));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> tooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("message", "File is too large"));
    }
}

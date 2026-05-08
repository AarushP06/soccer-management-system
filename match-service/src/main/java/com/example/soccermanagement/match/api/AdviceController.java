package com.example.soccermanagement.match.api;

import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.exception.MatchImportException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Maps application and domain exceptions to HTTP responses.
 */
@ControllerAdvice
public class AdviceController {
    @ExceptionHandler(MatchNotFoundException.class)
    public ResponseEntity<String> handleNotFound(MatchNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MatchConflictException.class)
    public ResponseEntity<String> handleConflict(MatchConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({MatchImportException.class})
    public ResponseEntity<String> handleImportErrors(MatchImportException ex) {
        if (isMissingResourceImportFailure(ex)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> handleExternalService(ExternalServiceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Upstream service unavailable: " + ex.getMessage());
    }

    @ExceptionHandler(com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException.class)
    public ResponseEntity<String> handleRelatedNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(com.example.soccermanagement.match.domain.exception.MatchValidationException.class)
    public ResponseEntity<String> handleValidation(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<String> handleInvalidInput(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    private boolean isMissingResourceImportFailure(MatchImportException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Stadium not found:")) {
            return true;
        }

        Throwable cause = ex.getCause();
        return cause instanceof MatchImportException
                && cause.getMessage() != null
                && cause.getMessage().contains("Stadium not found:");
    }
}

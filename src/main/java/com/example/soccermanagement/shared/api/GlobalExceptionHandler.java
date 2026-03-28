package com.example.soccermanagement.shared.api;

import com.example.soccermanagement.league.application.exception.LeagueConflictException;
import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.location.application.exception.StadiumConflictException;
import com.example.soccermanagement.location.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.shared.domain.DomainException;
import com.example.soccermanagement.team.application.exception.TeamConflictException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
    }

    @ExceptionHandler({
            LeagueNotFoundException.class,
            TeamNotFoundException.class,
            StadiumNotFoundException.class,
            MatchNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({
            LeagueConflictException.class,
            TeamConflictException.class,
            StadiumConflictException.class,
            MatchConflictException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(ExternalApiRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(ExternalApiRateLimitException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path, List<String> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        ));
    }
}

package com.example.soccermanagement.gateway.api;

import com.example.soccermanagement.gateway.application.exception.GatewayRouteException;
import com.example.soccermanagement.gateway.application.exception.GatewayServiceUnavailableException;
import com.example.soccermanagement.gateway.domain.exception.GatewayConfigurationException;
import com.example.soccermanagement.gateway.domain.exception.GatewayDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Maps application and domain exceptions to HTTP responses.
 */
@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(GatewayServiceUnavailableException.class)
    public ResponseEntity<String> handleServiceUnavailable(GatewayServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(GatewayRouteException.class)
    public ResponseEntity<String> handleRouteException(GatewayRouteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
    }

    @ExceptionHandler({GatewayConfigurationException.class, GatewayDomainException.class})
    public ResponseEntity<String> handleDomainExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

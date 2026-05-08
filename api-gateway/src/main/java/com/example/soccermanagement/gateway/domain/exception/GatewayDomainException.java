package com.example.soccermanagement.gateway.domain.exception;

/**
 * Represents an exception used in the gateway service.
 */
public class GatewayDomainException extends RuntimeException {
    public GatewayDomainException(String message) {
        super(message);
    }
}

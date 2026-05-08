package com.example.soccermanagement.gateway.application.exception;

/**
 * Represents an exception used in the gateway service.
 */
public class GatewayServiceUnavailableException extends RuntimeException {
    public GatewayServiceUnavailableException(String message) {
        super(message);
    }
}

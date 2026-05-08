package com.example.soccermanagement.gateway.domain.exception;

/**
 * Represents an exception used in the gateway service.
 */
public class GatewayConfigurationException extends RuntimeException {
    public GatewayConfigurationException(String message) {
        super(message);
    }
}

package com.example.soccermanagement.gateway.application.exception;

/**
 * Represents an exception used in the gateway service.
 */
public class GatewayRouteException extends RuntimeException {
    public GatewayRouteException(String message) {
        super(message);
    }
}

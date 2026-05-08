package com.example.soccermanagement.shared.domain;

/**
 * Represents an exception used in the shared service.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

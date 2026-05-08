package com.example.soccermanagement.stadium.domain.exception;

/**
 * Represents an exception used in the stadium service.
 */
public class StadiumValidationException extends RuntimeException {
    public StadiumValidationException(String message) {
        super(message);
    }
}

package com.example.soccermanagement.location.application.exception;

/**
 * Represents an exception used in the stadium service.
 */
public class StadiumConflictException extends RuntimeException {
    public StadiumConflictException(String message) {
        super(message);
    }
}


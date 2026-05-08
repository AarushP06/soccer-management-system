package com.example.soccermanagement.match.application.exception;

/**
 * Represents an exception used in the match service.
 */
public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(String message) {
        super(message);
    }
}


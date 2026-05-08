package com.example.soccermanagement.match.domain.exception;

/**
 * Represents an exception used in the match service.
 */
public class MatchValidationException extends RuntimeException {
    public MatchValidationException(String message) { super(message); }
}


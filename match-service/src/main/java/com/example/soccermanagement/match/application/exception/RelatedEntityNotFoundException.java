package com.example.soccermanagement.match.application.exception;

/**
 * Represents an exception used in the match service.
 */
public class RelatedEntityNotFoundException extends RuntimeException {
    public RelatedEntityNotFoundException(String message) { super(message); }
}


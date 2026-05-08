package com.example.soccermanagement.match.application.exception;

/**
 * Represents an exception used in the match service.
 */
public class MatchConflictException extends RuntimeException {
    public MatchConflictException(String message) { super(message); }
}


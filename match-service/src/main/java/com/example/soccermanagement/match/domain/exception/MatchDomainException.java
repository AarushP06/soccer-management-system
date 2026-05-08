package com.example.soccermanagement.match.domain.exception;

/**
 * Represents an exception used in the match service.
 */
public class MatchDomainException extends RuntimeException {
    public MatchDomainException(String message) { super(message); }
    public MatchDomainException(String message, Throwable cause) { super(message, cause); }
}


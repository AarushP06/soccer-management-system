package com.example.soccermanagement.match.domain.exception;

public class MatchDomainException extends RuntimeException {
    public MatchDomainException(String message) { super(message); }
    public MatchDomainException(String message, Throwable cause) { super(message, cause); }
}


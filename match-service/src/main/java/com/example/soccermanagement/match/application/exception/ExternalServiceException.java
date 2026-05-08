package com.example.soccermanagement.match.application.exception;

/**
 * Represents an exception used in the match service.
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) { super(message); }
    public ExternalServiceException(String message, Throwable cause) { super(message, cause); }
}


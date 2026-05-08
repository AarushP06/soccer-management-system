package com.example.soccermanagement.match.application.exception;

/**
 * Represents an exception used in the match service.
 */
public class MatchImportException extends RuntimeException {
    public MatchImportException(String message) { super(message); }
    public MatchImportException(String message, Throwable cause) { super(message, cause); }
}


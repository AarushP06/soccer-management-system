package com.example.soccermanagement.match.application.exception;

public class MatchImportException extends RuntimeException {
    public MatchImportException(String message) { super(message); }
    public MatchImportException(String message, Throwable cause) { super(message, cause); }
}


package com.example.soccermanagement.shared.exception;

public class ExternalApiRateLimitException extends RuntimeException {
    public ExternalApiRateLimitException(String message) { super(message); }
    public ExternalApiRateLimitException(String message, Throwable cause) { super(message, cause); }
}


package com.example.soccermanagement.stadium.application.exception;

/**
 * Represents an exception used in the stadium service.
 */
public class StadiumImportException extends RuntimeException {
    public StadiumImportException(String message) { super(message); }
    public StadiumImportException(String message, Throwable cause) { super(message, cause); }
}


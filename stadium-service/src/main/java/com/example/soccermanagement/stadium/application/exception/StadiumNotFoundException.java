package com.example.soccermanagement.stadium.application.exception;

/**
 * Represents an exception used in the stadium service.
 */
public class StadiumNotFoundException extends RuntimeException {
    public StadiumNotFoundException(String message) { super(message); }
}


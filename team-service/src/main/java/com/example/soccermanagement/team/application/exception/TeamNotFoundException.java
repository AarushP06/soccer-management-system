package com.example.soccermanagement.team.application.exception;

/**
 * Represents an exception used in the team service.
 */
public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(String message) { super(message); }
}


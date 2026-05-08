package com.example.soccermanagement.team.application.exception;

/**
 * Represents an exception used in the team service.
 */
public class TeamConflictException extends RuntimeException {
    public TeamConflictException(String message) {
        super(message);
    }
}


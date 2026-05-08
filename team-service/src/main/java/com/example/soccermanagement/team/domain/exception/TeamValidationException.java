package com.example.soccermanagement.team.domain.exception;

/**
 * Represents an exception used in the team service.
 */
public class TeamValidationException extends RuntimeException {
    public TeamValidationException(String message) {
        super(message);
    }
}

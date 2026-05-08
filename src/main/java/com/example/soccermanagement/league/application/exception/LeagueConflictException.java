package com.example.soccermanagement.league.application.exception;

/**
 * Represents an exception used in the league service.
 */
public class LeagueConflictException extends RuntimeException {
    public LeagueConflictException(String message) {
        super(message);
    }
}


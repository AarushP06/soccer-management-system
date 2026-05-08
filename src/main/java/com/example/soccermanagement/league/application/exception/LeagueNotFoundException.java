package com.example.soccermanagement.league.application.exception;

/**
 * Represents an exception used in the league service.
 */
public class LeagueNotFoundException extends RuntimeException {
    public LeagueNotFoundException(String message) {
        super(message);
    }
}


package com.example.soccermanagement.league.application.exception;

/**
 * Represents an exception used in the league service.
 */
public class LeagueImportException extends RuntimeException {
    public LeagueImportException(String message) { super(message); }
    public LeagueImportException(String message, Throwable cause) { super(message, cause); }
}


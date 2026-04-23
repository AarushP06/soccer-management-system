package com.example.soccermanagement.league.application.exception;

public class LeagueImportException extends RuntimeException {
    public LeagueImportException(String message) { super(message); }
    public LeagueImportException(String message, Throwable cause) { super(message, cause); }
}


package com.example.soccermanagement.location.application.exception;

public class StadiumNotFoundException extends RuntimeException {
    public StadiumNotFoundException(String message) {
        super(message);
    }
}


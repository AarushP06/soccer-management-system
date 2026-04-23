package com.example.soccermanagement.league.api;

import com.example.soccermanagement.league.application.exception.LeagueImportException;
import com.example.soccermanagement.league.application.exception.LeagueServiceException;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(LeagueNotFoundException.class)
    public ResponseEntity<String> handleNotFound(LeagueNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(LeagueConflictException.class)
    public ResponseEntity<String> handleConflict(LeagueConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({LeagueImportException.class, LeagueServiceException.class})
    public ResponseEntity<String> handleServiceErrors(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}


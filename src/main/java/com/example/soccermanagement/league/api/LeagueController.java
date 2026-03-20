package com.example.soccermanagement.league.api;

import com.example.soccermanagement.league.api.dto.CreateLeagueRequest;
import com.example.soccermanagement.league.api.dto.UpdateLeagueRequest;
import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.LeagueApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueApplicationService service;

    public LeagueController(LeagueApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<LeagueResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    public ResponseEntity<LeagueResponse> create(@Valid @RequestBody CreateLeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeagueResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateLeagueRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

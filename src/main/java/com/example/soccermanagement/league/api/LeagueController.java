package com.example.soccermanagement.league.api;

import com.example.soccermanagement.league.api.dto.CreateLeagueRequest;
import com.example.soccermanagement.league.api.dto.UpdateLeagueRequest;
import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.LeagueApplicationService;
import com.example.soccermanagement.league.application.LeagueImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
@Tag(name = "League", description = "League CRUD and import operations")
public class LeagueController {

    private final LeagueApplicationService service;
    private final LeagueImportService importService;

    public LeagueController(LeagueApplicationService service, LeagueImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    @Operation(summary = "Get all leagues", description = "Return a list of all local leagues")
    public ResponseEntity<List<LeagueResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a league", description = "Get a single league by its local UUID")
    public ResponseEntity<LeagueResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    @Operation(summary = "Create a new league", description = "Create a league with a local name")
    public ResponseEntity<LeagueResponse> create(@Valid @RequestBody CreateLeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/import/competition/{code}")
    @Operation(summary = "Import league by competition code", description = "Import a competition from football-data.org and persist it locally as a league. Examples: PL, PPL, SA")
    public ResponseEntity<LeagueResponse> importCompetition(@Parameter(description = "Competition code (example: PL)", example = "PL") @PathVariable String code) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importCompetition(code));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a league", description = "Rename an existing league")
    public ResponseEntity<LeagueResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateLeagueRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a league", description = "Delete a league by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

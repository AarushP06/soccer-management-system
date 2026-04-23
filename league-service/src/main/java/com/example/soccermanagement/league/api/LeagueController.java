package com.example.soccermanagement.league.api;

import com.example.soccermanagement.league.api.dto.LeagueRequest;
import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.LeagueImportService;
import com.example.soccermanagement.league.application.LeagueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leagues")
@Tag(name = "League API", description = "CRUD operations for leagues")
public class LeagueController {
    private final LeagueService leagueService;
    private final LeagueImportService leagueImportService;

    public LeagueController(LeagueService leagueService, LeagueImportService leagueImportService) {
        this.leagueService = leagueService;
        this.leagueImportService = leagueImportService;
    }

    @Operation(summary = "Create a new league")
    @PostMapping
    public ResponseEntity<LeagueResponse> create(@Valid @RequestBody LeagueRequest request) {
        var created = leagueService.create(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LeagueResponse(created.getId(), created.getName()));
    }

    @Operation(summary = "List all leagues")
    @GetMapping
    public ResponseEntity<List<LeagueResponse>> list() {
        var leagues = leagueService.list();
        var resp = leagues.stream().map(l -> new LeagueResponse(l.getId(), l.getName())).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Get a league by id")
    @GetMapping("/{id}")
    public ResponseEntity<LeagueResponse> get(@PathVariable Long id) {
        var league = leagueService.getById(id);
        return ResponseEntity.ok(new LeagueResponse(league.getId(), league.getName()));
    }

    @Operation(summary = "Update a league name")
    @PutMapping("/{id}")
    public ResponseEntity<LeagueResponse> update(@PathVariable Long id, @Valid @RequestBody LeagueRequest request) {
        var existing = leagueService.getById(id);
        existing.setName(request.name());
        var saved = leagueService.create(existing.getName());
        return ResponseEntity.ok(new LeagueResponse(saved.getId(), saved.getName()));
    }

    @Operation(summary = "Delete a league by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leagueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Import a competition by code from football-data.org")
    @PostMapping("/import/competition/{code}")
    public ResponseEntity<LeagueResponse> importCompetition(@PathVariable String code) {
        var response = leagueImportService.importByCompetitionCode(code);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

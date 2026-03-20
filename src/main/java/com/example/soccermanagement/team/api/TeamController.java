package com.example.soccermanagement.team.api;

import com.example.soccermanagement.team.api.dto.CreateTeamRequest;
import com.example.soccermanagement.team.api.dto.UpdateTeamRequest;
import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.application.TeamApplicationService;
import com.example.soccermanagement.team.application.TeamImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamApplicationService service;
    private final TeamImportService importService;

    public TeamController(TeamApplicationService service, TeamImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/import/competition/{code}")
    public ResponseEntity<TeamImportSummary> importTeams(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importTeamsByCompetitionCode(code));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

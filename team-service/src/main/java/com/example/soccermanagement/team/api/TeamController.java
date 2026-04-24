package com.example.soccermanagement.team.api;

import com.example.soccermanagement.team.api.dto.CreateTeamRequest;
import com.example.soccermanagement.team.api.dto.UpdateTeamRequest;
import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.application.TeamApplicationService;
import com.example.soccermanagement.team.application.TeamImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Team", description = "Team CRUD and import operations")
public class TeamController {

    private final TeamApplicationService service;
    private final TeamImportService importService;

    public TeamController(TeamApplicationService service, TeamImportService importService) {
        this.service = service;
        this.importService = importService;
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Return a list of all local teams")
    public ResponseEntity<List<TeamResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a team", description = "Get a single team by its local UUID")
    public ResponseEntity<TeamResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    @Operation(summary = "Create a new team", description = "Create a team with a local name")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create teams", description = "Create multiple teams in a single request")
    public ResponseEntity<List<TeamResponse>> bulkCreate(@Valid @RequestBody List<CreateTeamRequest> requests) {
        var created = new ArrayList<TeamResponse>();
        var existing = service.getAll();
        for (var r : requests) {
            try {
                created.add(service.create(r));
            } catch (com.example.soccermanagement.team.application.exception.TeamConflictException ex) {
                var found = existing.stream().filter(t -> t.name().equalsIgnoreCase(r.name())).findFirst();
                found.ifPresent(f -> created.add(f));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import/local")
    @Operation(summary = "Import teams from local JSON data", description = "Import teams from src/main/resources/data/teams.json. Each team record may include an explicit UUID 'id' to keep stable ids. Duplicates (by name) are skipped.",
            responses = {})
    public ResponseEntity<TeamImportSummary> importLocalTeams() {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importFromLocal());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team", description = "Rename an existing team")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team", description = "Delete a team by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

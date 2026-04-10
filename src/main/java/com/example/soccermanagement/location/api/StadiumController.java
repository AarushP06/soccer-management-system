package com.example.soccermanagement.location.api;

import com.example.soccermanagement.location.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.location.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.application.StadiumApplicationService;
import com.example.soccermanagement.location.application.StadiumImportService;
import com.example.soccermanagement.location.application.StadiumBulkImportService;
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
@RequestMapping("/api/stadiums")
@Tag(name = "Stadium", description = "Stadium CRUD and import operations")
public class StadiumController {

    private final StadiumApplicationService service;
    private final StadiumImportService importService;
    private final StadiumBulkImportService bulkImportService;

    public StadiumController(StadiumApplicationService service, StadiumImportService importService, StadiumBulkImportService bulkImportService) {
        this.service = service;
        this.importService = importService;
        this.bulkImportService = bulkImportService;
    }

    @GetMapping
    @Operation(summary = "Get all stadiums", description = "Return a list of local stadiums with optional external venue metadata")
    public ResponseEntity<List<StadiumResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a stadium", description = "Get a single stadium by its local UUID")
    public ResponseEntity<StadiumResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    @Operation(summary = "Create a new stadium", description = "Create a stadium with a local name")
    public ResponseEntity<StadiumResponse> create(@Valid @RequestBody CreateStadiumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/import/venue/{venueId}")
    @Operation(summary = "Import stadium by external venue id", description = "Import a stadium using an external API-Football venue id. Example: 670")
    public ResponseEntity<StadiumResponse> importVenue(@Parameter(description = "External API-Football venue id", example = "670") @PathVariable Integer venueId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importVenueByVenueId(venueId));
    }

    @PostMapping("/import/league/{leagueId}/season/{season}")
    @Operation(summary = "Bulk import stadiums from teams in a league", description = "Import stadiums discovered via API-Football teams for a given league and season. Creates stadiums only if they don't already exist locally.")
    public ResponseEntity<StadiumBulkImportService.StadiumBulkImportSummary> importFromLeague(@Parameter(description = "API-Football league id (integer)", example = "140") @PathVariable Integer leagueId, @Parameter(description = "Season year (integer)", example = "2026") @PathVariable Integer season) {
        var summary = bulkImportService.importFromLeagueTeams(leagueId, season);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a stadium", description = "Update a stadium's name or metadata")
    public ResponseEntity<StadiumResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateStadiumRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stadium", description = "Delete a stadium by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

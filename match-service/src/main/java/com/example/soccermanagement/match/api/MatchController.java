package com.example.soccermanagement.match.api;

import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.api.dto.MatchImportSummary;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.application.MatchApplicationService;
import com.example.soccermanagement.match.application.MatchImportService;
import com.example.soccermanagement.match.application.MatchOrchestrationService;
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
@RequestMapping("/api/matches")
@Tag(name = "Match", description = "Match CRUD, details and import operations")
public class MatchController {

    private final MatchApplicationService matchApplicationService;
    private final MatchOrchestrationService matchOrchestrationService;
    private final MatchImportService matchImportService;

    public MatchController(
            MatchApplicationService matchApplicationService,
            MatchOrchestrationService matchOrchestrationService,
            MatchImportService matchImportService
    ) {
        this.matchApplicationService = matchApplicationService;
        this.matchOrchestrationService = matchOrchestrationService;
        this.matchImportService = matchImportService;
    }

    @GetMapping
    @Operation(summary = "Get all matches", description = "Return a list of matches with local ids and names")
    public ResponseEntity<List<MatchResponse>> getAll() {
        return ResponseEntity.ok(matchApplicationService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a match", description = "Get a match by local UUID")
    public ResponseEntity<MatchResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(matchApplicationService.getOne(id));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get match details with HATEOAS links", description = "Get extended match details with HATEOAS links for navigation")
    public ResponseEntity<MatchDetailsResponse> getDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(matchOrchestrationService.getMatchDetails(id));
    }

    @PostMapping
    @Operation(summary = "Create a match", description = "Create a match linking existing local league and teams and a stadium")
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchApplicationService.create(request));
    }

    @PostMapping("/import/competition/{code}")
    @Operation(summary = "Import matches for a competition", description = "Import matches for a competition code and assign them to a local stadium. Example code: PPL")
    public ResponseEntity<MatchImportSummary> importMatches(
            @Parameter(description = "Competition code (example: PPL)", example = "PPL") @PathVariable String code,
            @Parameter(description = "Local stadiumId to assign imported matches to", example = "33333333-3333-3333-3333-333333333333") @RequestParam UUID stadiumId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchImportService.importMatchesByCompetitionCode(code, stadiumId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a match", description = "Delete a match by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        matchApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


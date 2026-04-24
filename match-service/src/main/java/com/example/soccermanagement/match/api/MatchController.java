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

import java.util.ArrayList;
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

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create matches", description = "Create multiple matches in a single request")
    public ResponseEntity<List<MatchResponse>> bulkCreate(@Valid @RequestBody List<CreateMatchRequest> requests) {
        var created = new ArrayList<MatchResponse>();

        for (var request : requests) {
            try {
                created.add(matchApplicationService.create(request));
            } catch (com.example.soccermanagement.match.application.exception.MatchConflictException ex) {
                try {
                    final UUID leagueUuid = request.leagueId() == null || request.leagueId().isBlank()
                            ? null
                            : UUID.fromString(request.leagueId());

                    final UUID homeUuid = UUID.fromString(request.homeTeamId());
                    final UUID awayUuid = UUID.fromString(request.awayTeamId());

                    var found = matchApplicationService.getAll().stream()
                            .filter(match ->
                                    match.homeTeamId().equals(homeUuid)
                                            && match.awayTeamId().equals(awayUuid)
                                            && (leagueUuid == null || match.leagueId().equals(leagueUuid))
                            )
                            .findFirst();

                    found.ifPresent(created::add);
                } catch (Exception ignored) {
                    // Continue with the next request
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import/local")
    @Operation(summary = "Import matches from local JSON data", description = "Import matches from src/main/resources/data/matches.json and assign them to a local stadium. Each match entry should include league (name), homeTeam (name), awayTeam (name). Returns a summary with imported/skipped counts.",
            responses = {})
    public ResponseEntity<MatchImportSummary> importLocalMatches(
            @Parameter(description = "Local stadiumId to assign imported matches to", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") @RequestParam UUID stadiumId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchImportService.importMatchesFromLocal(stadiumId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a match", description = "Delete a match by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        matchApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a match", description = "Update match fields (currently status)")
    public ResponseEntity<MatchResponse> update(@PathVariable UUID id, @Valid @RequestBody com.example.soccermanagement.match.api.dto.UpdateMatchRequest request) {
        return ResponseEntity.ok(matchApplicationService.update(id, request));
    }
}

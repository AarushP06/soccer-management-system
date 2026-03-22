package com.example.soccermanagement.match.api;

import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.api.dto.MatchImportSummary;
import com.example.soccermanagement.match.application.MatchApplicationService;
import com.example.soccermanagement.match.application.MatchImportService;
import com.example.soccermanagement.match.application.MatchOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
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
    public ResponseEntity<List<MatchResponse>> getAll() {
        return ResponseEntity.ok(matchApplicationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(matchApplicationService.getOne(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<MatchDetailsResponse> getDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(matchOrchestrationService.getMatchDetails(id));
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchApplicationService.create(request));
    }

    @PostMapping("/import/competition/{code}")
    public ResponseEntity<MatchImportSummary> importMatches(
            @PathVariable String code,
            @RequestParam UUID stadiumId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchImportService.importMatchesByCompetitionCode(code, stadiumId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        matchApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}



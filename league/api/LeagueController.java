// ...existing code...
import league.application.LeagueImportService;
// ...existing code...

/**
 * Exposes HTTP endpoints for application operations.
 */
@RestController
@RequestMapping("/api/leagues")
public class LeagueController {
    // ...existing code...
    private final LeagueImportService leagueImportService;

    public LeagueController(
            // ...existing params...,
            LeagueImportService leagueImportService
    ) {
        // ...existing code...
        this.leagueImportService = leagueImportService;
    }

    // ...existing code...

    @PostMapping("/import/competition/{code}")
    public ResponseEntity<LeagueResponse> importCompetition(@PathVariable String code) {
        LeagueResponse response = leagueImportService.importByCompetitionCode(code);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ...existing code...
}


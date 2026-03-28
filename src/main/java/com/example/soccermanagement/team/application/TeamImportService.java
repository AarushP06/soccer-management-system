package com.example.soccermanagement.team.application;

import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamImportService {

    private final FootballDataLeagueClient footballDataLeagueClient;
    private final TeamRepository repository;

    public TeamImportService(FootballDataLeagueClient footballDataLeagueClient,
                             TeamRepository repository) {
        this.footballDataLeagueClient = footballDataLeagueClient;
        this.repository = repository;
    }

    @Transactional
    public TeamImportSummary importTeamsByCompetitionCode(String code) {
        String normalizedCode = code == null ? "" : code.trim();
        // Verify competition exists first
        footballDataLeagueClient.getCompetitionByCode(normalizedCode)
                .orElseThrow(() -> new LeagueNotFoundException("Competition not found for code: " + normalizedCode));

        List<FootballDataLeagueClient.Team> externalTeams = footballDataLeagueClient.getTeamsByCompetitionCode(normalizedCode);

        int imported = 0;
        int skipped = 0;

        for (FootballDataLeagueClient.Team externalTeam : externalTeams) {
            if (externalTeam == null || externalTeam.name() == null || externalTeam.name().isBlank()) {
                skipped++;
                continue;
            }

            String teamName = externalTeam.name().trim();

            if (repository.existsByName(teamName)) {
                skipped++;
                continue;
            }

            repository.save(Team.create(teamName));
            imported++;
        }

        return new TeamImportSummary(imported, skipped);
    }
}

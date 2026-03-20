package com.example.soccermanagement.team.application;

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
        List<FootballDataLeagueClient.Team> externalTeams = footballDataLeagueClient.getTeamsByCompetitionCode(code);

        int imported = 0;
        int skipped = 0;

        for (FootballDataLeagueClient.Team externalTeam : externalTeams) {
            String teamName = externalTeam.name();

            if (repository.existsByName(teamName)) {
                skipped++;
            } else {
                Team saved = repository.save(Team.create(teamName));
                imported++;
            }
        }

        return new TeamImportSummary(imported, skipped);
    }
}


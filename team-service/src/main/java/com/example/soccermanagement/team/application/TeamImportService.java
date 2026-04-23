package com.example.soccermanagement.team.application;

import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import com.example.soccermanagement.team.application.exception.TeamImportLeagueNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class TeamImportService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String footballDataBaseUrl;
    private final TeamRepository repository;

    public TeamImportService(@Value("${external.football-data.url:https://api.football-data.org/v2}") String footballDataBaseUrl,
                             TeamRepository repository) {
        this.footballDataBaseUrl = footballDataBaseUrl;
        this.repository = repository;
    }

    @Transactional
    public TeamImportSummary importTeamsByCompetitionCode(String code) {
        String normalizedCode = code == null ? "" : code.trim();
        // Verify competition exists first
        try {
            CompetitionDto competition = restTemplate.getForObject(footballDataBaseUrl + "/competitions/{code}", CompetitionDto.class, normalizedCode);
            if (competition == null || competition.name == null) {
                throw new TeamImportLeagueNotFoundException("Competition not found for code: " + normalizedCode);
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new TeamImportLeagueNotFoundException("Competition not found for code: " + normalizedCode);
            }
            throw ex;
        }

        TeamDto[] arr;
        try {
            arr = restTemplate.getForObject(footballDataBaseUrl + "/competitions/{code}/teams", TeamDto[].class, normalizedCode);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                arr = new TeamDto[0];
            } else {
                throw ex;
            }
        }

        List<TeamDto> externalTeams = arr == null ? List.of() : Arrays.asList(arr);

        int imported = 0;
        int skipped = 0;

        for (TeamDto externalTeam : externalTeams) {
            if (externalTeam == null || externalTeam.name == null || externalTeam.name.isBlank()) {
                skipped++;
                continue;
            }

            String teamName = externalTeam.name.trim();
            String externalId = externalTeam.id;

            if (repository.existsByName(teamName)) {
                skipped++;
                continue;
            }

            repository.save(Team.createFromExternal(teamName, externalId));
            imported++;
        }

        return new TeamImportSummary(imported, skipped);
    }

    // local DTOs matching football-data responses
    public static class CompetitionDto { public String name; }
    public static class TeamDto { public String id; public String name; }
}

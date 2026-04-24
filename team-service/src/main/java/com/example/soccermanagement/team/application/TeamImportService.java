package com.example.soccermanagement.team.application;

import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
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
        boolean useSeedFallback = false;
        try {
            CompetitionDto competition = restTemplate.getForObject(footballDataBaseUrl + "/competitions/{code}", CompetitionDto.class, normalizedCode);
            if (competition == null || competition.name == null) {
                // fall back to seed
                useSeedFallback = true;
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                // fall back to seed if available, otherwise throw
                useSeedFallback = true;
            } else {
                throw ex;
            }
        }

        TeamDto[] arr;
        if (useSeedFallback) {
            // Attempt to load teams from classpath seeds/teams-{code}.json
            String resource = "seeds/teams-" + (normalizedCode.isBlank() ? "default" : normalizedCode) + ".json";
            try {
                ClassPathResource r = new ClassPathResource(resource);
                if (!r.exists()) {
                    arr = new TeamDto[0];
                } else {
                    try (InputStream is = r.getInputStream()) {
                        ObjectMapper mapper = new ObjectMapper();
                        arr = mapper.readValue(is, TeamDto[].class);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load seed resource: " + resource, e);
            }
        } else {
            try {
                arr = restTemplate.getForObject(footballDataBaseUrl + "/competitions/{code}/teams", TeamDto[].class, normalizedCode);
            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    arr = new TeamDto[0];
                } else {
                    throw ex;
                }
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

    @Transactional
    public TeamImportSummary importFromLocal() {
        String resource = "data/teams.json";
        try {
            ClassPathResource r = new ClassPathResource(resource);
            if (!r.exists()) throw new RuntimeException("Local teams data not found: " + resource);
            try (InputStream is = r.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                TeamDto[] arr = mapper.readValue(is, TeamDto[].class);
                if (arr == null) arr = new TeamDto[0];
                int imported = 0;
                int skipped = 0;
                for (TeamDto t : arr) {
                    if (t == null || t.name == null || t.name.isBlank()) { skipped++; continue; }
                    String name = t.name.trim();
                    if (repository.existsByName(name)) { skipped++; continue; }
                    if (t.id != null && !t.id.isBlank()) {
                        // try to parse UUID and rehydrate
                        try {
                            java.util.UUID uuid = java.util.UUID.fromString(t.id);
                            repository.save(Team.rehydrate(uuid, name, t.externalId));
                        } catch (Exception ex) {
                            // invalid UUID, create normally
                            repository.save(Team.createFromExternal(name, t.externalId));
                        }
                    } else {
                        repository.save(Team.createFromExternal(name, t.externalId));
                    }
                    imported++;
                }
                return new TeamImportSummary(imported, skipped);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to import local teams", ex);
        }
    }

    // local DTOs matching football-data responses
    public static class CompetitionDto { public String name; }
    public static class TeamDto { public String id; public String name; public String externalId; }
}

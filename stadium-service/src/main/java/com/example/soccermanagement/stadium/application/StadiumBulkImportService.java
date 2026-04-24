package com.example.soccermanagement.stadium.application;

import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.infrastructure.integration.ApiFootballVenueClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class StadiumBulkImportService {
    private final ApiFootballVenueClient apiFootballVenueClient;
    private final StadiumRepository stadiumRepository;

    public StadiumBulkImportService(ApiFootballVenueClient apiFootballVenueClient, StadiumRepository stadiumRepository) {
        this.apiFootballVenueClient = apiFootballVenueClient;
        this.stadiumRepository = stadiumRepository;
    }

    @Transactional
    public StadiumBulkImportSummary importFromLeagueTeams(Integer leagueId, Integer season) {
        var teams = apiFootballVenueClient.getTeamsByLeagueAndSeason(leagueId, season);
        int imported = 0;
        int skipped = 0;
        List<StadiumResponse> created = new ArrayList<>();
        for (ApiFootballVenueClient.Team t : teams.response()) {
            if (t == null || t.venue() == null) { skipped++; continue; }
            var v = t.venue();
            String name = v.name();
            if (name == null || name.isBlank()) { skipped++; continue; }
            if (stadiumRepository.existsByName(name)) { skipped++; continue; }
            var stadium = com.example.soccermanagement.stadium.domain.Stadium.createFromExternal(name, v.id(), v.city(), v.country(), v.capacity());
            stadiumRepository.save(stadium);
            imported++;
            created.add(StadiumApiMapper.toResponse(stadium));
        }
        return new StadiumBulkImportSummary(imported, skipped, created);
    }

    @Transactional
    public StadiumBulkImportSummary importFromLocal() {
        String resource = "data/stadiums.json";
        try {
            ClassPathResource r = new ClassPathResource(resource);
            if (!r.exists()) throw new RuntimeException("Local stadiums data not found: " + resource);
            try (InputStream is = r.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                LocalStadiumDto[] arr = mapper.readValue(is, LocalStadiumDto[].class);
                int imported = 0;
                int skipped = 0;
                List<StadiumResponse> created = new ArrayList<>();
                if (arr != null) {
                    for (LocalStadiumDto s : arr) {
                        if (s == null || s.name == null || s.name.isBlank()) { skipped++; continue; }
                        String name = s.name.trim();
                        if (stadiumRepository.existsByName(name)) { skipped++; continue; }
                        var stadium = com.example.soccermanagement.stadium.domain.Stadium.rehydrate(
                                s.id == null ? java.util.UUID.randomUUID() : java.util.UUID.fromString(s.id),
                                name,
                                s.externalVenueId,
                                s.city,
                                s.country,
                                s.capacity
                        );
                        stadiumRepository.save(stadium);
                        imported++;
                        created.add(StadiumApiMapper.toResponse(stadium));
                    }
                }
                return new StadiumBulkImportSummary(imported, skipped, created);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to import local stadiums", ex);
        }
    }

    public record StadiumBulkImportSummary(int imported, int skipped, List<StadiumResponse> created) {}

    private static class LocalStadiumDto { public String id; public String name; public Integer externalVenueId; public String city; public String country; public Integer capacity; }
}

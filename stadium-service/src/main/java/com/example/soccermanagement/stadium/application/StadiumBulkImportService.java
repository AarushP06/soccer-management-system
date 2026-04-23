package com.example.soccermanagement.stadium.application;

import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.infrastructure.integration.ApiFootballVenueClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public record StadiumBulkImportSummary(int imported, int skipped, List<StadiumResponse> created) {}
}


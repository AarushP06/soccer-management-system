package com.example.soccermanagement.location.application;

import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.location.application.port.StadiumRepository;
import com.example.soccermanagement.location.domain.Stadium;
import com.example.soccermanagement.location.infrastructure.integration.ApiFootballVenueClient;
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
        var teamsResponse = apiFootballVenueClient.getTeamsByLeagueAndSeason(leagueId, season);
        int imported = 0;
        int skipped = 0;
        List<StadiumResponse> created = new ArrayList<>();

        for (ApiFootballVenueClient.Team team : teamsResponse.response()) {
            if (team == null || team.venue() == null || team.venue().name() == null || team.venue().name().isBlank()) {
                skipped++;
                continue;
            }
            String stadiumName = team.venue().name().trim();
            Integer externalVenueId = team.venue().id();
            String city = team.venue().city();
            String country = team.venue().country();
            Integer capacity = team.venue().capacity();

            if (stadiumRepository.existsByName(stadiumName)) {
                skipped++;
                continue;
            }
            Stadium stadium = Stadium.createFromExternal(stadiumName, externalVenueId, city, country, capacity);
            Stadium saved = stadiumRepository.save(stadium);
            imported++;
            created.add(StadiumApiMapper.toResponse(saved));
        }

        return new StadiumBulkImportSummary(imported, skipped, created);
    }

    public record StadiumBulkImportSummary(int imported, int skipped, List<StadiumResponse> created) {
    }
}


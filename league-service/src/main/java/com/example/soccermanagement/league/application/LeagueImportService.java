package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class LeagueImportService {
    private final FootballDataLeagueClient footballDataLeagueClient;
    private final LeagueRepository leagueRepository;

    public LeagueImportService(FootballDataLeagueClient footballDataLeagueClient, LeagueRepository leagueRepository) {
        this.footballDataLeagueClient = footballDataLeagueClient;
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public LeagueResponse importByCompetitionCode(String code) {
        var competition = footballDataLeagueClient.getCompetitionByCode(code);
        String name = competition.name();
        if (leagueRepository.existsByName(name)) {
            throw new LeagueConflictException("League already exists with name: " + name);
        }
        League saved = leagueRepository.save(new League(null, name));
        return new LeagueResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public LeagueResponse importFromLocal() {
        try {
            ClassPathResource r = new ClassPathResource("data/leagues.json");
            if (!r.exists()) throw new RuntimeException("Local leagues data not found: data/leagues.json");
            try (InputStream is = r.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                LeagueDto[] arr = mapper.readValue(is, LeagueDto[].class);
                if (arr == null || arr.length == 0) throw new RuntimeException("No leagues found in data file");
                // import first valid league (or you can import all - return summary). We'll import all and return the last created as response.
                LeagueResponse last = null;
                for (LeagueDto dto : arr) {
                    if (dto == null || dto.name == null || dto.name.isBlank()) continue;
                    if (leagueRepository.existsByName(dto.name)) continue;
                    League saved = leagueRepository.save(new League(dto.id, dto.name));
                    last = new LeagueResponse(saved.getId(), saved.getName());
                }
                if (last == null) throw new LeagueConflictException("No new leagues to import");
                return last;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to import local leagues", ex);
        }
    }

    private static class LeagueDto { public Long id; public String name; }
}

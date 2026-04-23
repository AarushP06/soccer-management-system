package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}


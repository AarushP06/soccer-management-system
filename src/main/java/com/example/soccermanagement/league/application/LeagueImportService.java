package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.api.mapper.LeagueApiMapper;
import com.example.soccermanagement.league.application.exception.LeagueConflictException;
import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeagueImportService {

    private final FootballDataLeagueClient footballDataLeagueClient;
    private final LeagueRepository repository;

    public LeagueImportService(FootballDataLeagueClient footballDataLeagueClient,
                               LeagueRepository repository) {
        this.footballDataLeagueClient = footballDataLeagueClient;
        this.repository = repository;
    }

    @Transactional
    public LeagueResponse importCompetition(String code) {
        var externalCompetition = footballDataLeagueClient.getCompetitionByCode(code)
                .orElseThrow(() -> new LeagueNotFoundException("Competition not found for code: " + code));

        String leagueName = externalCompetition.name();

        if (repository.existsByName(leagueName)) {
            throw new LeagueConflictException("League already exists: " + leagueName);
        }

        League saved = repository.save(League.create(leagueName));
        return LeagueApiMapper.toResponse(saved);
    }
}


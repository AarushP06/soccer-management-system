package com.example.soccermanagement.match.application;

import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.location.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.api.mapper.MatchApiMapper;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MatchApplicationService {

    private final MatchRepository repository;
    private final LeagueLookupPort leagueLookupPort;
    private final TeamLookupPort teamLookupPort;
    private final StadiumLookupPort stadiumLookupPort;

    public MatchApplicationService(
            MatchRepository repository,
            LeagueLookupPort leagueLookupPort,
            TeamLookupPort teamLookupPort,
            StadiumLookupPort stadiumLookupPort
    ) {
        this.repository = repository;
        this.leagueLookupPort = leagueLookupPort;
        this.teamLookupPort = teamLookupPort;
        this.stadiumLookupPort = stadiumLookupPort;
    }

    public List<MatchResponse> getAll() {
        return repository.findAll().stream().map(match -> {
            String leagueName = leagueLookupPort.findNameById(match.getLeagueId()).orElse(null);
            String externalLeagueCode = leagueLookupPort.findExternalCodeById(match.getLeagueId()).orElse(null);
            String homeName = teamLookupPort.findNameById(match.getHomeTeamId()).orElse(null);
            String externalHomeId = teamLookupPort.findExternalIdById(match.getHomeTeamId()).orElse(null);
            String awayName = teamLookupPort.findNameById(match.getAwayTeamId()).orElse(null);
            String externalAwayId = teamLookupPort.findExternalIdById(match.getAwayTeamId()).orElse(null);
            String stadiumName = stadiumLookupPort.findNameById(match.getStadiumId()).orElse(null);
            Integer externalVenueId = stadiumLookupPort.findExternalVenueIdById(match.getStadiumId()).orElse(null);
            return MatchApiMapper.toResponse(match, match.getExternalMatchId(), leagueName, externalLeagueCode, homeName, externalHomeId, awayName, externalAwayId, stadiumName, externalVenueId);
        }).toList();
    }

    public MatchResponse getOne(UUID id) {
        Match match = repository.findById(id).orElseThrow(() -> new MatchNotFoundException("Match not found"));
        String leagueName = leagueLookupPort.findNameById(match.getLeagueId()).orElse(null);
        String externalLeagueCode = leagueLookupPort.findExternalCodeById(match.getLeagueId()).orElse(null);
        String homeName = teamLookupPort.findNameById(match.getHomeTeamId()).orElse(null);
        String externalHomeId = teamLookupPort.findExternalIdById(match.getHomeTeamId()).orElse(null);
        String awayName = teamLookupPort.findNameById(match.getAwayTeamId()).orElse(null);
        String externalAwayId = teamLookupPort.findExternalIdById(match.getAwayTeamId()).orElse(null);
        String stadiumName = stadiumLookupPort.findNameById(match.getStadiumId()).orElse(null);
        Integer externalVenueId = stadiumLookupPort.findExternalVenueIdById(match.getStadiumId()).orElse(null);
        return MatchApiMapper.toResponse(match, match.getExternalMatchId(), leagueName, externalLeagueCode, homeName, externalHomeId, awayName, externalAwayId, stadiumName, externalVenueId);
    }

    public MatchResponse create(CreateMatchRequest request) {
        if (!leagueLookupPort.existsById(request.leagueId())) {
            throw new LeagueNotFoundException("League not found");
        }
        if (!teamLookupPort.existsById(request.homeTeamId()) || !teamLookupPort.existsById(request.awayTeamId())) {
            throw new TeamNotFoundException("One or both teams not found");
        }
        if (!stadiumLookupPort.existsById(request.stadiumId())) {
            throw new StadiumNotFoundException("Stadium not found");
        }

        try {
            Match match = Match.create(request.leagueId(), request.homeTeamId(), request.awayTeamId(), request.stadiumId());
            Match saved = repository.save(match);
            String leagueName = leagueLookupPort.findNameById(saved.getLeagueId()).orElse(null);
            String externalLeagueCode = leagueLookupPort.findExternalCodeById(saved.getLeagueId()).orElse(null);
            String homeName = teamLookupPort.findNameById(saved.getHomeTeamId()).orElse(null);
            String externalHomeId = teamLookupPort.findExternalIdById(saved.getHomeTeamId()).orElse(null);
            String awayName = teamLookupPort.findNameById(saved.getAwayTeamId()).orElse(null);
            String externalAwayId = teamLookupPort.findExternalIdById(saved.getAwayTeamId()).orElse(null);
            String stadiumName = stadiumLookupPort.findNameById(saved.getStadiumId()).orElse(null);
            Integer externalVenueId = stadiumLookupPort.findExternalVenueIdById(saved.getStadiumId()).orElse(null);
            return MatchApiMapper.toResponse(saved, saved.getExternalMatchId(), leagueName, externalLeagueCode, homeName, externalHomeId, awayName, externalAwayId, stadiumName, externalVenueId);
        } catch (DataIntegrityViolationException ex) {
            throw new MatchConflictException("Match conflict: duplicate or invalid state");
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}

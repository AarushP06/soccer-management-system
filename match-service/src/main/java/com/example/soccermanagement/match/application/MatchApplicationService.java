package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.api.dto.UpdateMatchRequest;
import com.example.soccermanagement.match.api.mapper.MatchApiMapper;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.domain.Match;
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

    public MatchApplicationService(MatchRepository repository, LeagueLookupPort leagueLookupPort, TeamLookupPort teamLookupPort, StadiumLookupPort stadiumLookupPort) {
        this.repository = repository;
        this.leagueLookupPort = leagueLookupPort;
        this.teamLookupPort = teamLookupPort;
        this.stadiumLookupPort = stadiumLookupPort;
    }

    public List<MatchResponse> getAll() {
        try {
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
        } catch (ExternalServiceException ex) {
            throw ex; // handled by AdviceController -> 503
        }
    }

    public MatchResponse getOne(UUID id) {
        try {
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
        } catch (ExternalServiceException ex) {
            throw ex;
        }
    }

    public MatchResponse create(CreateMatchRequest request) {
        try {
            // parse ids: league may be numeric id (Long) or UUID string - try to handle both
            java.util.UUID leagueUuid = null;
            java.util.UUID homeUuid = java.util.UUID.fromString(request.homeTeamId());
            java.util.UUID awayUuid = java.util.UUID.fromString(request.awayTeamId());
            java.util.UUID stadiumUuid = java.util.UUID.fromString(request.stadiumId());
            boolean leagueExists = false;
            try {
                // try as UUID
                leagueUuid = java.util.UUID.fromString(request.leagueId());
                leagueExists = leagueLookupPort.existsById(leagueUuid);
            } catch (Exception e) {
                // not a UUID, try numeric lookup via integration that accepts string id in findByName etc.
                leagueExists = leagueLookupPort.findByName(request.leagueId()).isPresent();
            }
            if (!leagueExists) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("League not found: " + request.leagueId());
            }
            if (!teamLookupPort.existsById(homeUuid) || !teamLookupPort.existsById(awayUuid)) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("One or both teams not found");
            }
            if (!stadiumLookupPort.existsById(stadiumUuid)) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("Stadium not found: " + request.stadiumId());
            }

            try {
                // create domain match using UUIDs (use leagueUuid if available, else null)
                Match match = Match.create(leagueUuid != null ? leagueUuid : java.util.UUID.randomUUID(), homeUuid, awayUuid, stadiumUuid);
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
        } catch (ExternalServiceException ex) {
            throw ex; // let AdviceController map to 503
        }
    }

    public MatchResponse update(UUID id, UpdateMatchRequest request) {
        Match existing = repository.findById(id).orElseThrow(() -> new com.example.soccermanagement.match.application.exception.MatchNotFoundException("Match not found"));
        // only status update for now
        Match updated = Match.rehydrate(existing.getId(), existing.getLeagueId(), existing.getHomeTeamId(), existing.getAwayTeamId(), existing.getStadiumId(), existing.getExternalMatchId(), request.status());
        Match saved = repository.save(updated);
        String leagueName = leagueLookupPort.findNameById(saved.getLeagueId()).orElse(null);
        String externalLeagueCode = leagueLookupPort.findExternalCodeById(saved.getLeagueId()).orElse(null);
        String homeName = teamLookupPort.findNameById(saved.getHomeTeamId()).orElse(null);
        String externalHomeId = teamLookupPort.findExternalIdById(saved.getHomeTeamId()).orElse(null);
        String awayName = teamLookupPort.findNameById(saved.getAwayTeamId()).orElse(null);
        String externalAwayId = teamLookupPort.findExternalIdById(saved.getAwayTeamId()).orElse(null);
        String stadiumName = stadiumLookupPort.findNameById(saved.getStadiumId()).orElse(null);
        Integer externalVenueId = stadiumLookupPort.findExternalVenueIdById(saved.getStadiumId()).orElse(null);
        return MatchApiMapper.toResponse(saved, saved.getExternalMatchId(), leagueName, externalLeagueCode, homeName, externalHomeId, awayName, externalAwayId, stadiumName, externalVenueId);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}

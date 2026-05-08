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
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Coordinates application use cases and cross-component workflow for match operations.
 */
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
            UUID leagueUuid = resolveLeagueId(request.leagueId());
            UUID homeUuid = UUID.fromString(request.homeTeamId());
            UUID awayUuid = UUID.fromString(request.awayTeamId());
            UUID stadiumUuid = UUID.fromString(request.stadiumId());

            if (!leagueLookupPort.existsById(leagueUuid)) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("League not found: " + request.leagueId());
            }
            if (!teamLookupPort.existsById(homeUuid) || !teamLookupPort.existsById(awayUuid)) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("One or both teams not found");
            }
            if (!stadiumLookupPort.existsById(stadiumUuid)) {
                throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("Stadium not found: " + request.stadiumId());
            }
            if (repository.existsByLeagueAndTeams(leagueUuid, homeUuid, awayUuid)) {
                throw new MatchConflictException("Match conflict: duplicate or invalid state");
            }

            try {
                Match match = Match.create(leagueUuid, homeUuid, awayUuid, stadiumUuid);
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

    private UUID resolveLeagueId(String leagueReference) {
        if (leagueReference == null || leagueReference.isBlank()) {
            throw new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("League not found: " + leagueReference);
        }

        try {
            UUID leagueUuid = UUID.fromString(leagueReference);
            if (leagueLookupPort.existsById(leagueUuid)) {
                return leagueUuid;
            }
        } catch (IllegalArgumentException ignored) {
            // Try alternate formats below.
        }

        try {
            long numericId = Long.parseLong(leagueReference.trim());
            return LeagueReferenceMapper.toInternalUuid(numericId);
        } catch (NumberFormatException ignored) {
            // Try a name lookup below.
        }

        return leagueLookupPort.findByName(leagueReference.trim())
                .map(com.example.soccermanagement.match.application.dto.LeagueInfo::id)
                .orElseThrow(() -> new com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException("League not found: " + leagueReference));
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

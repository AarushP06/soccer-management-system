package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.dto.MatchImportSummary;
import com.example.soccermanagement.match.application.exception.MatchImportException;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.infrastructure.integration.FootballDataMatchClient;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class MatchImportService {
    private final FootballDataMatchClient footballDataMatchClient;
    private final MatchRepository matchRepository;
    private final LeagueLookupPort leagueLookupPort;
    private final TeamLookupPort teamLookupPort;
    private final StadiumLookupPort stadiumLookupPort;

    public MatchImportService(FootballDataMatchClient footballDataMatchClient,
                              MatchRepository matchRepository,
                              LeagueLookupPort leagueLookupPort,
                              TeamLookupPort teamLookupPort,
                              StadiumLookupPort stadiumLookupPort) {
        this.footballDataMatchClient = footballDataMatchClient;
        this.matchRepository = matchRepository;
        this.leagueLookupPort = leagueLookupPort;
        this.teamLookupPort = teamLookupPort;
        this.stadiumLookupPort = stadiumLookupPort;
    }

    @Transactional
    public MatchImportSummary importMatchesByCompetitionCode(String code, UUID stadiumId) {
        String normalizedCode = code == null ? "" : code.trim();
        try {
            if (!stadiumLookupPort.existsById(stadiumId)) {
                throw new com.example.soccermanagement.match.application.exception.MatchImportException("Stadium not found: " + stadiumId);
            }

            var competition = footballDataMatchClient.getCompetitionByCode(normalizedCode);
            if (competition == null) {
                return new MatchImportSummary(normalizedCode, stadiumId, 0, 0, 0, 1);
            }

            var leagueOpt = leagueLookupPort.findByName(competition.name());
            if (leagueOpt.isEmpty()) {
                return new MatchImportSummary(normalizedCode, stadiumId, 0, 0, 0, 1);
            }
            UUID leagueId = leagueOpt.get().id();

            List<FootballDataMatchClient.ExternalMatch> externalMatches = footballDataMatchClient.getMatchesByCompetitionCode(normalizedCode);

            int imported = 0;
            int skipped = 0;
            int missingTeams = 0;
            int missingLeague = 0;

            for (FootballDataMatchClient.ExternalMatch externalMatch : externalMatches) {
                if (externalMatch == null
                        || externalMatch.homeTeam() == null
                        || externalMatch.awayTeam() == null
                        || externalMatch.homeTeam().name() == null
                        || externalMatch.awayTeam().name() == null
                        || externalMatch.homeTeam().name().isBlank()
                        || externalMatch.awayTeam().name().isBlank()) {
                    skipped++;
                    continue;
                }

                String homeTeamName = externalMatch.homeTeam().name().trim();
                String awayTeamName = externalMatch.awayTeam().name().trim();
                var homeTeam = teamLookupPort.findByName(homeTeamName);
                if (homeTeam.isEmpty()) {
                    missingTeams++;
                    skipped++;
                    continue;
                }
                var awayTeam = teamLookupPort.findByName(awayTeamName);
                if (awayTeam.isEmpty()) {
                    missingTeams++;
                    skipped++;
                    continue;
                }
                UUID homeTeamId = homeTeam.get().id();
                UUID awayTeamId = awayTeam.get().id();
                if (matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)) {
                    skipped++;
                    continue;
                }
                String externalMatchId = null;
                try { externalMatchId = externalMatch.id(); } catch (Exception ignored) {}
                Match match = externalMatchId != null ? Match.createFromExternal(leagueId, homeTeamId, awayTeamId, stadiumId, externalMatchId)
                        : Match.create(leagueId, homeTeamId, awayTeamId, stadiumId);
                matchRepository.save(match);
                imported++;
            }

            return new MatchImportSummary(normalizedCode, stadiumId, imported, skipped, missingTeams, missingLeague);
        } catch (RuntimeException ex) {
            throw new MatchImportException("Failed to import matches: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ExternalServiceException("External error during import", ex);
        }
    }

    @Transactional
    public MatchImportSummary importMatchesFromLocal(UUID stadiumId) {
        try {
            if (!stadiumLookupPort.existsById(stadiumId)) {
                throw new com.example.soccermanagement.match.application.exception.MatchImportException("Stadium not found: " + stadiumId);
            }
            String resource = "data/matches.json";
            ClassPathResource r = new ClassPathResource(resource);
            if (!r.exists()) {
                return new MatchImportSummary("local", stadiumId, 0, 0, 0, 1);
            }
            try (InputStream is = r.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                LocalMatchDto[] arr = mapper.readValue(is, LocalMatchDto[].class);
                int imported = 0;
                int skipped = 0;
                int missingTeams = 0;
                int missingLeague = 0;
                for (LocalMatchDto dto : arr) {
                    if (dto == null || dto.homeTeam == null || dto.awayTeam == null || dto.homeTeam.isBlank() || dto.awayTeam.isBlank()) {
                        skipped++; continue;
                    }
                    var homeOpt = teamLookupPort.findByName(dto.homeTeam.trim());
                    var awayOpt = teamLookupPort.findByName(dto.awayTeam.trim());
                    if (homeOpt.isEmpty() || awayOpt.isEmpty()) {
                        missingTeams++; skipped++; continue;
                    }
                    UUID homeTeamId = homeOpt.get().id();
                    UUID awayTeamId = awayOpt.get().id();
                    // league lookup
                    var leagueOpt = leagueLookupPort.findByName(dto.league == null ? "" : dto.league.trim());
                    if (leagueOpt.isEmpty()) { missingLeague++; skipped++; continue; }
                    UUID leagueId = leagueOpt.get().id();
                    if (matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)) { skipped++; continue; }
                    Match match = dto.externalId != null && !dto.externalId.isBlank()
                            ? Match.createFromExternal(leagueId, homeTeamId, awayTeamId, stadiumId, dto.externalId)
                            : Match.create(leagueId, homeTeamId, awayTeamId, stadiumId);
                    matchRepository.save(match);
                    imported++;
                }
                return new MatchImportSummary("local", stadiumId, imported, skipped, missingTeams, missingLeague);
            }
        } catch (RuntimeException ex) {
            throw new MatchImportException("Failed to import matches: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ExternalServiceException("External error during import", ex);
        }
    }

    private static class LocalMatchDto { public String league; public String homeTeam; public String awayTeam; public String externalId; }
}

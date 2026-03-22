package com.example.soccermanagement.match.application;
import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import com.example.soccermanagement.location.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.location.application.port.StadiumRepository;
import com.example.soccermanagement.match.api.dto.MatchImportSummary;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.infrastructure.integration.FootballDataMatchClient;
import com.example.soccermanagement.team.application.port.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Service
public class MatchImportService {
    private final FootballDataMatchClient footballDataMatchClient;
    private final FootballDataLeagueClient footballDataLeagueClient;
    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    public MatchImportService(
            FootballDataMatchClient footballDataMatchClient,
            FootballDataLeagueClient footballDataLeagueClient,
            MatchRepository matchRepository,
            LeagueRepository leagueRepository,
            TeamRepository teamRepository,
            StadiumRepository stadiumRepository
    ) {
        this.footballDataMatchClient = footballDataMatchClient;
        this.footballDataLeagueClient = footballDataLeagueClient;
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.stadiumRepository = stadiumRepository;
    }
    @Transactional
    public MatchImportSummary importMatchesByCompetitionCode(String code, UUID stadiumId) {
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new StadiumNotFoundException("Stadium not found: " + stadiumId);
        }
        var competition = footballDataLeagueClient.getCompetitionByCode(code)
                .orElseThrow(() -> new LeagueNotFoundException("Competition not found for code: " + code));
        var leagueOpt = leagueRepository.findByName(competition.name());
        if (leagueOpt.isEmpty()) {
            return new MatchImportSummary(code, stadiumId, 0, 0, 0, 1);
        }
        UUID leagueId = leagueOpt.get().getId();
        List<FootballDataMatchClient.ExternalMatch> externalMatches = footballDataMatchClient.getMatchesByCompetitionCode(code);
        int imported = 0;
        int skipped = 0;
        int missingTeams = 0;
        int missingLeague = 0;
        for (FootballDataMatchClient.ExternalMatch externalMatch : externalMatches) {
            String homeTeamName = externalMatch.homeTeam().name();
            String awayTeamName = externalMatch.awayTeam().name();
            var homeTeam = teamRepository.findByName(homeTeamName);
            if (homeTeam.isEmpty()) {
                missingTeams++;
                skipped++;
                continue;
            }
            var awayTeam = teamRepository.findByName(awayTeamName);
            if (awayTeam.isEmpty()) {
                missingTeams++;
                skipped++;
                continue;
            }
            UUID homeTeamId = homeTeam.get().getId();
            UUID awayTeamId = awayTeam.get().getId();
            if (matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)) {
                skipped++;
                continue;
            }
            try {
                Match match = Match.create(leagueId, homeTeamId, awayTeamId, stadiumId);
                matchRepository.save(match);
                imported++;
            } catch (Exception ex) {
                skipped++;
            }
        }
        return new MatchImportSummary(code, stadiumId, imported, skipped, missingTeams, missingLeague);
    }
}
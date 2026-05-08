package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.dto.MatchImportSummary;
import com.example.soccermanagement.match.application.dto.LeagueInfo;
import com.example.soccermanagement.match.application.dto.TeamInfo;
import com.example.soccermanagement.match.application.exception.MatchImportException;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.infrastructure.integration.FootballDataMatchClient;
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies application-layer branches and use-case behavior for the match service.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("testing")
class MatchImportServiceTest {

    @Mock
    private FootballDataMatchClient footballDataMatchClient;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private LeagueLookupPort leagueLookupPort;

    @Mock
    private TeamLookupPort teamLookupPort;

    @Mock
    private StadiumLookupPort stadiumLookupPort;

    @InjectMocks
    private MatchImportService service;

    @Test
    void importMatchesByCompetitionCodeImportsAndSkipsExpectedEntries() {
        UUID stadiumId = UUID.randomUUID();
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(footballDataMatchClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataMatchClient.CompetitionDto("Premier League"));
        when(footballDataMatchClient.getMatchesByCompetitionCode("PL"))
                .thenReturn(List.of(
                        new FootballDataMatchClient.ExternalMatch("m1", new FootballDataMatchClient.Team("57", "Arsenal"), new FootballDataMatchClient.Team("64", "Liverpool")),
                        new FootballDataMatchClient.ExternalMatch("m2", new FootballDataMatchClient.Team("57", "Arsenal"), null),
                        new FootballDataMatchClient.ExternalMatch("m3", new FootballDataMatchClient.Team("57", "Arsenal"), new FootballDataMatchClient.Team("61", "Chelsea"))
                ));
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.of(new LeagueInfo(leagueId, "Premier League", "PL")));
        when(teamLookupPort.findByName("Arsenal")).thenReturn(Optional.of(new TeamInfo(homeTeamId, "Arsenal", "57")));
        when(teamLookupPort.findByName("Liverpool")).thenReturn(Optional.of(new TeamInfo(awayTeamId, "Liverpool", "64")));
        when(teamLookupPort.findByName("Chelsea")).thenReturn(Optional.empty());
        when(matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)).thenReturn(false);
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MatchImportSummary summary = service.importMatchesByCompetitionCode(" PL ", stadiumId);

        assertThat(summary.competitionCode()).isEqualTo("PL");
        assertThat(summary.imported()).isEqualTo(1);
        assertThat(summary.skipped()).isEqualTo(2);
        assertThat(summary.missingTeams()).isEqualTo(1);
        assertThat(summary.missingLeague()).isEqualTo(0);
    }

    @Test
    void importMatchesByCompetitionCodeReturnsMissingLeagueSummaryWhenLeagueLookupFails() {
        UUID stadiumId = UUID.randomUUID();
        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(footballDataMatchClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataMatchClient.CompetitionDto("Premier League"));
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.empty());

        MatchImportSummary summary = service.importMatchesByCompetitionCode("PL", stadiumId);

        assertThat(summary.imported()).isZero();
        assertThat(summary.missingLeague()).isEqualTo(1);
        verify(matchRepository, never()).save(any());
    }

    @Test
    void importMatchesByCompetitionCodeReturnsMissingLeagueWhenCompetitionIsMissing() {
        UUID stadiumId = UUID.randomUUID();
        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(footballDataMatchClient.getCompetitionByCode("PL")).thenReturn(null);

        MatchImportSummary summary = service.importMatchesByCompetitionCode("PL", stadiumId);

        assertThat(summary.imported()).isZero();
        assertThat(summary.missingLeague()).isEqualTo(1);
        assertThat(summary.skipped()).isZero();
    }

    @Test
    void importMatchesByCompetitionCodeSkipsDuplicatesAndWrapsMissingStadium() {
        UUID stadiumId = UUID.randomUUID();
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true, false);
        when(footballDataMatchClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataMatchClient.CompetitionDto("Premier League"));
        when(footballDataMatchClient.getMatchesByCompetitionCode("PL"))
                .thenReturn(List.of(new FootballDataMatchClient.ExternalMatch(null,
                        new FootballDataMatchClient.Team("57", "Arsenal"),
                        new FootballDataMatchClient.Team("64", "Liverpool"))));
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.of(new LeagueInfo(leagueId, "Premier League", "PL")));
        when(teamLookupPort.findByName("Arsenal")).thenReturn(Optional.of(new TeamInfo(homeTeamId, "Arsenal", "57")));
        when(teamLookupPort.findByName("Liverpool")).thenReturn(Optional.of(new TeamInfo(awayTeamId, "Liverpool", "64")));
        when(matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)).thenReturn(true);

        MatchImportSummary summary = service.importMatchesByCompetitionCode("PL", stadiumId);

        assertThat(summary.imported()).isZero();
        assertThat(summary.skipped()).isEqualTo(1);
        verify(matchRepository, never()).save(any());

        assertThatThrownBy(() -> service.importMatchesByCompetitionCode("PL", stadiumId))
                .isInstanceOf(MatchImportException.class)
                .hasMessageContaining("Stadium not found");
    }

    @Test
    void importMatchesFromLocalUsesSeedFileAndThrowsWhenStadiumMissing() {
        UUID stadiumId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID away = UUID.fromString("22222222-2222-2222-2222-222222222222");

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(teamLookupPort.findByName("Manchester City")).thenReturn(Optional.of(new TeamInfo(home, "Manchester City", "65")));
        when(teamLookupPort.findByName("Liverpool")).thenReturn(Optional.of(new TeamInfo(away, "Liverpool", "64")));
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.of(new LeagueInfo(leagueId, "Premier League", "PL")));
        when(matchRepository.existsByLeagueAndTeams(leagueId, home, away)).thenReturn(false);
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MatchImportSummary summary = service.importMatchesFromLocal(stadiumId);

        assertThat(summary.imported()).isEqualTo(1);
        assertThat(summary.skipped()).isZero();

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(false);

        assertThatThrownBy(() -> service.importMatchesFromLocal(stadiumId))
                .isInstanceOf(MatchImportException.class)
                .hasMessageContaining("Stadium not found");
    }

    @Test
    void importMatchesFromLocalCountsMissingLeagueAndDuplicates() {
        UUID stadiumId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID away = UUID.fromString("22222222-2222-2222-2222-222222222222");

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true, true);
        when(teamLookupPort.findByName("Manchester City"))
                .thenReturn(Optional.of(new TeamInfo(home, "Manchester City", "65")))
                .thenReturn(Optional.of(new TeamInfo(home, "Manchester City", "65")));
        when(teamLookupPort.findByName("Liverpool"))
                .thenReturn(Optional.of(new TeamInfo(away, "Liverpool", "64")))
                .thenReturn(Optional.of(new TeamInfo(away, "Liverpool", "64")));
        when(leagueLookupPort.findByName("Premier League"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new LeagueInfo(leagueId, "Premier League", "PL")));
        when(matchRepository.existsByLeagueAndTeams(leagueId, home, away)).thenReturn(true);

        MatchImportSummary missingLeague = service.importMatchesFromLocal(stadiumId);
        MatchImportSummary duplicate = service.importMatchesFromLocal(stadiumId);

        assertThat(missingLeague.missingLeague()).isEqualTo(1);
        assertThat(missingLeague.skipped()).isEqualTo(1);
        assertThat(duplicate.imported()).isZero();
        assertThat(duplicate.skipped()).isEqualTo(1);
    }

    @Test
    void importMatchesFromLocalCountsMissingTeams() {
        UUID stadiumId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID away = UUID.fromString("22222222-2222-2222-2222-222222222222");

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(teamLookupPort.findByName("Manchester City")).thenReturn(Optional.empty());
        when(teamLookupPort.findByName("Liverpool")).thenReturn(Optional.of(new TeamInfo(away, "Liverpool", "64")));

        MatchImportSummary summary = service.importMatchesFromLocal(stadiumId);

        assertThat(summary.imported()).isZero();
        assertThat(summary.skipped()).isEqualTo(1);
        assertThat(summary.missingTeams()).isEqualTo(1);
    }

    @Test
    void importMatchesByCompetitionCodeWrapsRepositoryFailures() {
        UUID stadiumId = UUID.randomUUID();
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();

        when(stadiumLookupPort.existsById(stadiumId)).thenReturn(true);
        when(footballDataMatchClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataMatchClient.CompetitionDto("Premier League"));
        when(footballDataMatchClient.getMatchesByCompetitionCode("PL"))
                .thenReturn(List.of(new FootballDataMatchClient.ExternalMatch("m1",
                        new FootballDataMatchClient.Team("57", "Arsenal"),
                        new FootballDataMatchClient.Team("64", "Liverpool"))));
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.of(new LeagueInfo(leagueId, "Premier League", "PL")));
        when(teamLookupPort.findByName("Arsenal")).thenReturn(Optional.of(new TeamInfo(homeTeamId, "Arsenal", "57")));
        when(teamLookupPort.findByName("Liverpool")).thenReturn(Optional.of(new TeamInfo(awayTeamId, "Liverpool", "64")));
        when(matchRepository.existsByLeagueAndTeams(leagueId, homeTeamId, awayTeamId)).thenReturn(false);
        when(matchRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.importMatchesByCompetitionCode("PL", stadiumId))
                .isInstanceOf(MatchImportException.class)
                .hasMessageContaining("db down");
    }
}

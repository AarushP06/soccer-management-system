package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.api.dto.UpdateMatchRequest;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.domain.exception.MatchValidationException;
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
class MatchApplicationServiceTest {

    @Mock private MatchRepository repository;
    @Mock private LeagueLookupPort leagueLookupPort;
    @Mock private TeamLookupPort teamLookupPort;
    @Mock private StadiumLookupPort stadiumLookupPort;

    @InjectMocks
    private MatchApplicationService service;

    @Test
    void createMatchReturnsSavedResponse() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        Match saved = Match.rehydrate(UUID.randomUUID(), leagueId, home, away, stadium, null, "SCHEDULED");

        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(true);
        when(repository.existsByLeagueAndTeams(leagueId, home, away)).thenReturn(false);
        when(repository.save(any(Match.class))).thenReturn(saved);
        stubNames(saved);

        MatchResponse response = service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString()));

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.leagueName()).isEqualTo("Premier League");
        assertThat(response.homeTeamName()).isEqualTo("Arsenal");
    }

    @Test
    void getAllMatchesReturnsMappedResponses() {
        Match match = Match.rehydrate(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "m1", "SCHEDULED");
        when(repository.findAll()).thenReturn(List.of(match));
        stubNames(match);

        List<MatchResponse> responses = service.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stadiumName()).isEqualTo("Old Trafford");
    }

    @Test
    void getOneThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOne(id))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessage("Match not found");
    }

    @Test
    void getOneReturnsMappedResponseWhenSupportingNamesAreMissing() {
        Match match = Match.rehydrate(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "m1", "SCHEDULED");
        when(repository.findById(match.getId())).thenReturn(Optional.of(match));
        when(leagueLookupPort.findNameById(match.getLeagueId())).thenReturn(Optional.empty());
        when(leagueLookupPort.findExternalCodeById(match.getLeagueId())).thenReturn(Optional.empty());
        when(teamLookupPort.findNameById(match.getHomeTeamId())).thenReturn(Optional.empty());
        when(teamLookupPort.findExternalIdById(match.getHomeTeamId())).thenReturn(Optional.empty());
        when(teamLookupPort.findNameById(match.getAwayTeamId())).thenReturn(Optional.empty());
        when(teamLookupPort.findExternalIdById(match.getAwayTeamId())).thenReturn(Optional.empty());
        when(stadiumLookupPort.findNameById(match.getStadiumId())).thenReturn(Optional.empty());
        when(stadiumLookupPort.findExternalVenueIdById(match.getStadiumId())).thenReturn(Optional.empty());

        MatchResponse response = service.getOne(match.getId());

        assertThat(response.id()).isEqualTo(match.getId());
        assertThat(response.leagueName()).isNull();
        assertThat(response.homeTeamName()).isNull();
        assertThat(response.awayTeamName()).isNull();
        assertThat(response.stadiumName()).isNull();
    }

    @Test
    void updateMatchReturnsUpdatedResponse() {
        Match existing = Match.rehydrate(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "SCHEDULED");
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubNames(existing);

        MatchResponse response = service.update(existing.getId(), new UpdateMatchRequest("CANCELLED"));

        assertThat(response.status()).isEqualTo("CANCELLED");
        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void duplicateMatchReturns409AndSkipsSave() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(true);
        when(repository.existsByLeagueAndTeams(leagueId, home, away)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(MatchConflictException.class)
                .hasMessage("Match conflict: duplicate or invalid state");

        verify(repository, never()).save(any(Match.class));
    }

    @Test
    void downstreamSupportingService404ReturnsNotFound() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(RelatedEntityNotFoundException.class)
                .hasMessage("One or both teams not found");

        verify(repository, never()).save(any(Match.class));
    }

    @Test
    void downstreamSupportingService503PropagatesExternalServiceException() {
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        when(leagueLookupPort.existsById(LeagueReferenceMapper.toInternalUuid(100L)))
                .thenThrow(new ExternalServiceException("League service unavailable"));

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("League service unavailable");

        verify(repository, never()).save(any(Match.class));
    }

    @Test
    void getAllPropagatesExternalServiceExceptionFromLookup() {
        Match match = Match.rehydrate(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "SCHEDULED");
        when(repository.findAll()).thenReturn(List.of(match));
        when(leagueLookupPort.findNameById(match.getLeagueId())).thenThrow(new ExternalServiceException("League service unavailable"));

        assertThatThrownBy(() -> service.getAll())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("League service unavailable");
    }

    @Test
    void repositorySaveIsNotCalledWhenValidationFails() {
        UUID team = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(team)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(true);
        when(repository.existsByLeagueAndTeams(leagueId, team, team)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", team.toString(), team.toString(), stadium.toString())))
                .isInstanceOf(MatchValidationException.class)
                .hasMessage("Home and away teams must be different");

        verify(repository, never()).save(any(Match.class));
    }

    @Test
    void dataIntegrityViolationMapsToConflict() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(true);
        when(repository.existsByLeagueAndTeams(leagueId, home, away)).thenReturn(false);
        when(repository.save(any(Match.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(MatchConflictException.class)
                .hasMessage("Match conflict: duplicate or invalid state");
    }

    @Test
    void createResolvesLeagueFromNameFallback() {
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        UUID fallbackLeagueId = UUID.randomUUID();
        Match fallbackSaved = Match.rehydrate(UUID.randomUUID(), fallbackLeagueId, home, away, stadium, "m8", "SCHEDULED");
        when(leagueLookupPort.existsById(fallbackLeagueId)).thenReturn(true);
        when(leagueLookupPort.findByName("Premier League")).thenReturn(Optional.of(new com.example.soccermanagement.match.application.dto.LeagueInfo(fallbackLeagueId, "Premier League", "PL")));
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(true);
        when(repository.existsByLeagueAndTeams(fallbackLeagueId, home, away)).thenReturn(false);
        when(repository.save(any(Match.class))).thenReturn(fallbackSaved);
        stubNames(fallbackSaved);

        MatchResponse fallbackResponse = service.create(new CreateMatchRequest("Premier League", home.toString(), away.toString(), stadium.toString()));

        assertThat(fallbackResponse.id()).isEqualTo(fallbackSaved.getId());
    }

    @Test
    void createThrowsNotFoundForBlankLeagueAndMissingStadium() {
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(new CreateMatchRequest(" ", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(RelatedEntityNotFoundException.class)
                .hasMessage("League not found:  ");

        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        when(leagueLookupPort.existsById(leagueId)).thenReturn(true);
        when(teamLookupPort.existsById(home)).thenReturn(true);
        when(teamLookupPort.existsById(away)).thenReturn(true);
        when(stadiumLookupPort.existsById(stadium)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateMatchRequest("100", home.toString(), away.toString(), stadium.toString())))
                .isInstanceOf(RelatedEntityNotFoundException.class)
                .hasMessage("Stadium not found: " + stadium);
    }

    @Test
    void updateThrowsNotFoundWhenMatchDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UpdateMatchRequest("FINISHED")))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessage("Match not found");
    }

    private void stubNames(Match match) {
        when(leagueLookupPort.findNameById(match.getLeagueId())).thenReturn(Optional.of("Premier League"));
        when(leagueLookupPort.findExternalCodeById(match.getLeagueId())).thenReturn(Optional.empty());
        when(teamLookupPort.findNameById(match.getHomeTeamId())).thenReturn(Optional.of("Arsenal"));
        when(teamLookupPort.findExternalIdById(match.getHomeTeamId())).thenReturn(Optional.empty());
        when(teamLookupPort.findNameById(match.getAwayTeamId())).thenReturn(Optional.of("Liverpool"));
        when(teamLookupPort.findExternalIdById(match.getAwayTeamId())).thenReturn(Optional.empty());
        when(stadiumLookupPort.findNameById(match.getStadiumId())).thenReturn(Optional.of("Old Trafford"));
        when(stadiumLookupPort.findExternalVenueIdById(match.getStadiumId())).thenReturn(Optional.empty());
    }
}

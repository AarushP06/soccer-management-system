package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Verifies application-layer branches and use-case behavior for the match service.
 */
@ExtendWith(MockitoExtension.class)
class MatchOrchestrationServiceTest {

    @Mock
    private MatchRepository repository;
    @Mock
    private LeagueLookupPort leagueLookupPort;
    @Mock
    private TeamLookupPort teamLookupPort;
    @Mock
    private StadiumLookupPort stadiumLookupPort;

    @InjectMocks
    private MatchOrchestrationService service;

    @Test
    void getMatchDetailsAggregatesNamesAndLinks() {
        UUID id = UUID.randomUUID();
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        UUID stadium = UUID.randomUUID();
        Match match = Match.rehydrate(id, leagueId, home, away, stadium, "m1", "SCHEDULED");

        when(repository.findById(id)).thenReturn(Optional.of(match));
        when(leagueLookupPort.findNameById(leagueId)).thenReturn(Optional.of("Premier League"));
        when(teamLookupPort.findNameById(home)).thenReturn(Optional.of("Arsenal"));
        when(teamLookupPort.findNameById(away)).thenReturn(Optional.of("Liverpool"));
        when(stadiumLookupPort.findNameById(stadium)).thenReturn(Optional.of("Old Trafford"));

        MatchDetailsResponse response = service.getMatchDetails(id);

        assertThat(response.getLeagueName()).isEqualTo("Premier League");
        assertThat(response.getHomeTeamName()).isEqualTo("Arsenal");
        assertThat(response.getAwayTeamName()).isEqualTo("Liverpool");
        assertThat(response.getStadiumName()).isEqualTo("Old Trafford");
        assertThat(response.getLinks().stream().map(link -> link.getRel().value()).toList())
                .contains("self", "matches");
    }

    @Test
    void getMatchDetailsThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMatchDetails(id))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessage("Match not found");
    }
}

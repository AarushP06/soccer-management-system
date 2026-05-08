package com.example.soccermanagement.match.domain;

import com.example.soccermanagement.match.domain.exception.MatchValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests expected behavior and edge cases in the match service.
 */
class MatchTest {

    @Test
    void createBuildsScheduledMatch() {
        Match match = Match.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThat(match.getId()).isNotNull();
        assertThat(match.getStatus()).isEqualTo("SCHEDULED");
        assertThat(match.getExternalMatchId()).isNull();
    }

    @Test
    void createFromExternalKeepsExternalId() {
        Match match = Match.createFromExternal(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "m1");

        assertThat(match.getExternalMatchId()).isEqualTo("m1");
        assertThat(match.getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void rehydrateRestoresState() {
        UUID id = UUID.randomUUID();
        UUID leagueId = UUID.randomUUID();
        UUID homeTeamId = UUID.randomUUID();
        UUID awayTeamId = UUID.randomUUID();
        UUID stadiumId = UUID.randomUUID();

        Match match = Match.rehydrate(id, leagueId, homeTeamId, awayTeamId, stadiumId, "ext-1", "FINISHED");

        assertThat(match.getId()).isEqualTo(id);
        assertThat(match.getLeagueId()).isEqualTo(leagueId);
        assertThat(match.getHomeTeamId()).isEqualTo(homeTeamId);
        assertThat(match.getAwayTeamId()).isEqualTo(awayTeamId);
        assertThat(match.getStadiumId()).isEqualTo(stadiumId);
        assertThat(match.getExternalMatchId()).isEqualTo("ext-1");
        assertThat(match.getStatus()).isEqualTo("FINISHED");
    }

    @Test
    void createRejectsSameTeams() {
        UUID teamId = UUID.randomUUID();

        assertThatThrownBy(() -> Match.create(UUID.randomUUID(), teamId, teamId, UUID.randomUUID()))
                .isInstanceOf(MatchValidationException.class)
                .hasMessage("Home and away teams must be different");
    }

    @Test
    void createRejectsNullTeams() {
        assertThatThrownBy(() -> Match.create(UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(MatchValidationException.class)
                .hasMessage("Team ids must not be null");
    }
}

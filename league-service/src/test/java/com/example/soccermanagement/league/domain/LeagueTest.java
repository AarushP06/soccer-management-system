package com.example.soccermanagement.league.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests expected behavior and edge cases in the league service.
 */
class LeagueTest {

    @Test
    void constructorAndGettersExposeState() {
        League league = new League(10L, "Premier League");

        assertThat(league.getId()).isEqualTo(10L);
        assertThat(league.getName()).isEqualTo("Premier League");
    }

    @Test
    void settersUpdateState() {
        League league = new League();

        league.setId(20L);
        league.setName("La Liga");

        assertThat(league.getId()).isEqualTo(20L);
        assertThat(league.getName()).isEqualTo("La Liga");
    }
}

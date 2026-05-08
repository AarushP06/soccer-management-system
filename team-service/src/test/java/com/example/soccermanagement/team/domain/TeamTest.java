package com.example.soccermanagement.team.domain;

import com.example.soccermanagement.team.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests expected behavior and edge cases in the team service.
 */
class TeamTest {

    @Test
    void createBuildsTeamWithGeneratedId() {
        Team team = Team.create("Arsenal");

        assertThat(team.getId()).isNotNull();
        assertThat(team.getName()).isEqualTo("Arsenal");
        assertThat(team.getExternalId()).isNull();
    }

    @Test
    void createFromExternalKeepsExternalId() {
        Team team = Team.createFromExternal("Benfica", "65");

        assertThat(team.getId()).isNotNull();
        assertThat(team.getName()).isEqualTo("Benfica");
        assertThat(team.getExternalId()).isEqualTo("65");
    }

    @Test
    void rehydrateRestoresState() {
        UUID id = UUID.randomUUID();

        Team team = Team.rehydrate(id, "Liverpool", "64");

        assertThat(team.getId()).isEqualTo(id);
        assertThat(team.getName()).isEqualTo("Liverpool");
        assertThat(team.getExternalId()).isEqualTo("64");
    }

    @Test
    void renameUpdatesName() {
        Team team = Team.create("Ajax");

        team.rename("Ajax Updated");

        assertThat(team.getName()).isEqualTo("Ajax Updated");
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Team.create(" "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Team name cannot be blank");
    }

    @Test
    void renameRejectsBlankName() {
        Team team = Team.create("Inter Milan");

        assertThatThrownBy(() -> team.rename(""))
                .isInstanceOf(DomainException.class)
                .hasMessage("Team name cannot be blank");
    }
}

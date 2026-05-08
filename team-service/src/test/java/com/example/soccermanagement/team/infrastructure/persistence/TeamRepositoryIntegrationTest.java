package com.example.soccermanagement.team.infrastructure.persistence;

import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises persistence integration behavior for the team service.
 */
@DataJpaTest
@ActiveProfiles("testing")
@Import(TeamRepositoryAdapter.class)
class TeamRepositoryIntegrationTest {

    @Autowired
    private TeamRepository repository;

    @Autowired
    private SpringDataTeamRepository springDataTeamRepository;

    @Test
    void saveAndFindByIdPersistTeamInH2() {
        Team saved = repository.save(Team.rehydrate(UUID.randomUUID(), "Arsenal", null));

        Optional<Team> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getName()).isEqualTo("Arsenal");
    }

    @Test
    void existsAndFindByNameReflectStoredTeam() {
        Team saved = repository.save(Team.rehydrate(UUID.randomUUID(), "Liverpool", "64"));

        assertThat(repository.existsByName("Liverpool")).isTrue();
        assertThat(repository.findByName("Liverpool"))
                .map(Team::getId)
                .contains(saved.getId());
    }

    @Test
    void uniqueConstraintRejectsDuplicateTeamNames() {
        TeamJpaEntity first = new TeamJpaEntity();
        first.setId(UUID.randomUUID());
        first.setName("Ajax");
        springDataTeamRepository.saveAndFlush(first);

        TeamJpaEntity duplicate = new TeamJpaEntity();
        duplicate.setId(UUID.randomUUID());
        duplicate.setName("Ajax");

        assertThatThrownBy(() -> springDataTeamRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteRemovesTeam() {
        Team saved = repository.save(Team.rehydrate(UUID.randomUUID(), "Benfica", null));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}

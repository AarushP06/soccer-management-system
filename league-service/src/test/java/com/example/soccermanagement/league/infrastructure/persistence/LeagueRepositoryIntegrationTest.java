package com.example.soccermanagement.league.infrastructure.persistence;

import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises persistence integration behavior for the league service.
 */
@DataJpaTest
@ActiveProfiles("testing")
@Import(LeagueRepositoryAdapter.class)
class LeagueRepositoryIntegrationTest {

    @Autowired
    private SpringDataLeagueRepository springDataLeagueRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Test
    void saveAndFindByIdPersistLeagueInH2() {
        League saved = leagueRepository.save(new League(null, "Premier League"));

        Optional<League> found = leagueRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getName()).isEqualTo("Premier League");
    }

    @Test
    void existsQueriesReflectStoredLeagues() {
        League saved = leagueRepository.save(new League(null, "La Liga"));

        assertThat(leagueRepository.existsByName("La Liga")).isTrue();
        assertThat(leagueRepository.existsByNameAndIdNot("La Liga", saved.getId())).isFalse();
        assertThat(leagueRepository.existsByNameAndIdNot("La Liga", -1L)).isTrue();
        assertThat(leagueRepository.findByName("La Liga"))
                .map(League::getId)
                .contains(saved.getId());
    }

    @Test
    void uniqueConstraintRejectsDuplicateLeagueNames() {
        springDataLeagueRepository.saveAndFlush(new LeagueEntity(null, "Serie A"));

        assertThatThrownBy(() -> springDataLeagueRepository.saveAndFlush(new LeagueEntity(null, "Serie A")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteRemovesLeague() {
        League saved = leagueRepository.save(new League(null, "Bundesliga"));

        leagueRepository.deleteById(saved.getId());

        assertThat(leagueRepository.findById(saved.getId())).isEmpty();
    }
}

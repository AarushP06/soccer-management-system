package com.example.soccermanagement.match.infrastructure.persistence;

import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
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
 * Exercises persistence integration behavior for the match service.
 */
@DataJpaTest
@ActiveProfiles("testing")
@Import(MatchRepositoryAdapter.class)
class MatchRepositoryIntegrationTest {

    @Autowired
    private MatchRepository repository;

    @Autowired
    private SpringDataMatchRepository springDataMatchRepository;

    @Test
    void saveAndFindByIdPersistMatchInH2() {
        Match saved = repository.save(Match.rehydrate(
                UUID.randomUUID(),
                LeagueReferenceMapper.toInternalUuid(100L),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "m1",
                "SCHEDULED"
        ));

        Optional<Match> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getExternalMatchId()).isEqualTo("m1");
    }

    @Test
    void existsByLeagueAndTeamsReflectsStoredMatch() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();
        repository.save(Match.rehydrate(UUID.randomUUID(), leagueId, home, away, UUID.randomUUID(), null, "SCHEDULED"));

        assertThat(repository.existsByLeagueAndTeams(leagueId, home, away)).isTrue();
    }

    @Test
    void uniqueConstraintRejectsDuplicateLeagueAndTeams() {
        UUID leagueId = LeagueReferenceMapper.toInternalUuid(100L);
        UUID home = UUID.randomUUID();
        UUID away = UUID.randomUUID();

        MatchJpaEntity first = new MatchJpaEntity();
        first.setId(UUID.randomUUID());
        first.setLeagueId(leagueId);
        first.setHomeTeamId(home);
        first.setAwayTeamId(away);
        first.setStadiumId(UUID.randomUUID());
        first.setStatus("SCHEDULED");
        springDataMatchRepository.saveAndFlush(first);

        MatchJpaEntity duplicate = new MatchJpaEntity();
        duplicate.setId(UUID.randomUUID());
        duplicate.setLeagueId(leagueId);
        duplicate.setHomeTeamId(home);
        duplicate.setAwayTeamId(away);
        duplicate.setStadiumId(UUID.randomUUID());
        duplicate.setStatus("SCHEDULED");

        assertThatThrownBy(() -> springDataMatchRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

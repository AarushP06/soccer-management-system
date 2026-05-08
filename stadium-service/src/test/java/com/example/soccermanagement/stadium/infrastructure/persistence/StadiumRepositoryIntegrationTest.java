package com.example.soccermanagement.stadium.infrastructure.persistence;

import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.domain.Stadium;
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
 * Exercises persistence integration behavior for the stadium service.
 */
@DataJpaTest
@ActiveProfiles("testing")
@Import(StadiumRepositoryAdapter.class)
class StadiumRepositoryIntegrationTest {

    @Autowired
    private StadiumRepository repository;

    @Autowired
    private SpringDataStadiumRepository springDataStadiumRepository;

    @Test
    void saveAndFindByIdPersistStadiumInH2() {
        Stadium saved = repository.save(Stadium.rehydrate(UUID.randomUUID(), "Old Trafford", 1, "Manchester", "England", 74879));

        Optional<Stadium> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getName()).isEqualTo("Old Trafford");
    }

    @Test
    void existsQueriesReflectStoredStadium() {
        Stadium saved = repository.save(Stadium.rehydrate(UUID.randomUUID(), "Anfield", 2, "Liverpool", "England", 54074));

        assertThat(repository.existsById(saved.getId())).isTrue();
        assertThat(repository.existsByName("Anfield")).isTrue();
    }

    @Test
    void uniqueConstraintRejectsDuplicateStadiumNames() {
        StadiumJpaEntity first = new StadiumJpaEntity();
        first.setId(UUID.randomUUID());
        first.setName("San Siro");
        springDataStadiumRepository.saveAndFlush(first);

        StadiumJpaEntity duplicate = new StadiumJpaEntity();
        duplicate.setId(UUID.randomUUID());
        duplicate.setName("San Siro");

        assertThatThrownBy(() -> springDataStadiumRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteRemovesStadium() {
        Stadium saved = repository.save(Stadium.rehydrate(UUID.randomUUID(), "Camp Nou", 3, "Barcelona", "Spain", 99354));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}

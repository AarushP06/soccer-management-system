package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies application-layer branches and use-case behavior for the league service.
 */
@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    @InjectMocks
    private LeagueService leagueService;

    @Test
    void createSavesLeagueWhenNameIsUnique() {
        when(leagueRepository.existsByName("Premier League")).thenReturn(false);
        when(leagueRepository.save(argThat(league ->
                league.getId() == null && "Premier League".equals(league.getName()))))
                .thenReturn(new League(1L, "Premier League"));

        League created = leagueService.create("Premier League");

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Premier League");
        verify(leagueRepository).existsByName("Premier League");
        verify(leagueRepository).save(argThat(league ->
                league.getId() == null && "Premier League".equals(league.getName())));
    }

    @Test
    void createThrowsConflictWhenLeagueAlreadyExists() {
        when(leagueRepository.existsByName("Premier League")).thenReturn(true);

        assertThatThrownBy(() -> leagueService.create("Premier League"))
                .isInstanceOf(LeagueConflictException.class)
                .hasMessage("League already exists with name: Premier League");
    }

    @Test
    void getByIdReturnsLeagueWhenPresent() {
        League league = new League(2L, "La Liga");
        when(leagueRepository.findById(2L)).thenReturn(Optional.of(league));

        League found = leagueService.getById(2L);

        assertThat(found).isSameAs(league);
    }

    @Test
    void getByIdThrowsNotFoundWhenMissing() {
        when(leagueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueService.getById(99L))
                .isInstanceOf(LeagueNotFoundException.class)
                .hasMessage("League not found: 99");
    }

    @Test
    void updateSavesUpdatedLeagueWhenNoOtherLeagueHasName() {
        League league = new League(3L, "Serie A");
        when(leagueRepository.findById(3L)).thenReturn(Optional.of(league));
        when(leagueRepository.existsByNameAndIdNot("Serie A Updated", 3L)).thenReturn(false);
        when(leagueRepository.save(league)).thenReturn(league);

        League updated = leagueService.update(3L, "  Serie A Updated  ");

        assertThat(updated.getName()).isEqualTo("Serie A Updated");
        verify(leagueRepository).existsByNameAndIdNot("Serie A Updated", 3L);
        verify(leagueRepository).save(league);
    }

    @Test
    void updateThrowsConflictWhenAnotherLeagueAlreadyHasName() {
        League league = new League(4L, "Bundesliga");
        when(leagueRepository.findById(4L)).thenReturn(Optional.of(league));
        when(leagueRepository.existsByNameAndIdNot("Premier League", 4L)).thenReturn(true);

        assertThatThrownBy(() -> leagueService.update(4L, "Premier League"))
                .isInstanceOf(LeagueConflictException.class)
                .hasMessage("League already exists with name: Premier League");
    }

    @Test
    void deleteDelegatesToRepository() {
        leagueService.delete(5L);

        verify(leagueRepository).deleteById(5L);
    }

    @Test
    void listReturnsAllLeagues() {
        List<League> leagues = List.of(new League(1L, "Premier League"), new League(2L, "La Liga"));
        when(leagueRepository.findAll()).thenReturn(leagues);

        assertThat(leagueService.list()).containsExactlyElementsOf(leagues);
    }

    @Test
    void updateNormalizesNullNameAndFindByNameDelegatesToRepository() {
        League league = new League(7L, "Serie A");
        when(leagueRepository.findById(7L)).thenReturn(Optional.of(league));
        when(leagueRepository.existsByNameAndIdNot("", 7L)).thenReturn(false);
        when(leagueRepository.save(league)).thenReturn(league);
        when(leagueRepository.findByName("Premier League")).thenReturn(Optional.of(new League(1L, "Premier League")));

        League updated = leagueService.update(7L, null);

        assertThat(updated.getName()).isEqualTo("");
        assertThat(leagueService.findByName("Premier League")).hasValueSatisfying(found ->
                assertThat(found.getName()).isEqualTo("Premier League"));
    }
}

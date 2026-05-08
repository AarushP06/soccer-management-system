package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.infrastructure.integration.FootballDataLeagueClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies application-layer branches and use-case behavior for the league service.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("testing")
class LeagueImportServiceTest {

    @Mock
    private FootballDataLeagueClient footballDataLeagueClient;

    @Mock
    private LeagueRepository leagueRepository;

    @InjectMocks
    private LeagueImportService service;

    @Test
    void importByCompetitionCodeCreatesLeagueWhenNameIsUnique() {
        when(footballDataLeagueClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataLeagueClient.CompetitionDto("Premier League"));
        when(leagueRepository.existsByName("Premier League")).thenReturn(false);
        when(leagueRepository.save(any(League.class))).thenReturn(new League(1L, "Premier League"));

        LeagueResponse response = service.importByCompetitionCode("PL");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Premier League");
    }

    @Test
    void importByCompetitionCodeThrowsConflictWhenLeagueAlreadyExists() {
        when(footballDataLeagueClient.getCompetitionByCode("PL"))
                .thenReturn(new FootballDataLeagueClient.CompetitionDto("Premier League"));
        when(leagueRepository.existsByName("Premier League")).thenReturn(true);

        assertThatThrownBy(() -> service.importByCompetitionCode("PL"))
                .isInstanceOf(LeagueConflictException.class)
                .hasMessage("League already exists with name: Premier League");
    }

    @Test
    void importFromLocalReturnsLastImportedLeagueAndSkipsExistingOnes() {
        when(leagueRepository.existsByName("Premier League")).thenReturn(true);
        when(leagueRepository.existsByName("La Liga")).thenReturn(false);
        when(leagueRepository.save(ArgumentMatchers.argThat(league ->
                league.getId() == 200L && "La Liga".equals(league.getName())
        ))).thenReturn(new League(200L, "La Liga"));

        LeagueResponse response = service.importFromLocal();

        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.name()).isEqualTo("La Liga");
    }

    @Test
    void importFromLocalThrowsConflictWhenNothingNewCanBeImported() {
        when(leagueRepository.existsByName("Premier League")).thenReturn(true);
        when(leagueRepository.existsByName("La Liga")).thenReturn(true);

        assertThatThrownBy(service::importFromLocal)
                .isInstanceOf(LeagueConflictException.class)
                .hasMessage("No new leagues to import");
    }

    @Test
    void importFromLocalSkipsBlankEntriesAndThrowsWhenRepositoryFails() {
        when(leagueRepository.existsByName("Premier League")).thenReturn(false);
        when(leagueRepository.save(any(League.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(service::importFromLocal)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }
}

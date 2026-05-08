package com.example.soccermanagement.stadium.application;

import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.application.exception.StadiumConflictException;
import com.example.soccermanagement.stadium.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.domain.Stadium;
import com.example.soccermanagement.stadium.infrastructure.integration.ApiFootballVenueClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests expected behavior and edge cases in the stadium service.
 */
@ActiveProfiles("testing")
class StadiumImportCoverageTest {

    @Test
    void importVenueByVenueIdCreatesNewStadium() {
        ApiFootballVenueClient apiFootballVenueClient = mock(ApiFootballVenueClient.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        StadiumImportService service = new StadiumImportService(apiFootballVenueClient, stadiumRepository);
        when(apiFootballVenueClient.getVenueById(1))
                .thenReturn(Optional.of(new ApiFootballVenueClient.ExternalVenue(1, "Old Trafford", "Manchester", "England", 74879)));
        when(stadiumRepository.existsByName("Old Trafford")).thenReturn(false);
        when(stadiumRepository.save(any(Stadium.class)))
                .thenReturn(Stadium.rehydrate(UUID.randomUUID(), "Old Trafford", null, null, null, null));

        StadiumResponse response = service.importVenueByVenueId(1);

        assertThat(response.name()).isEqualTo("Old Trafford");
    }

    @Test
    void importVenueByVenueIdThrowsWhenDuplicateOrMissing() {
        ApiFootballVenueClient apiFootballVenueClient = mock(ApiFootballVenueClient.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        StadiumImportService service = new StadiumImportService(apiFootballVenueClient, stadiumRepository);
        when(apiFootballVenueClient.getVenueById(1))
                .thenReturn(Optional.of(new ApiFootballVenueClient.ExternalVenue(1, "Old Trafford", "Manchester", "England", 74879)));
        when(stadiumRepository.existsByName("Old Trafford")).thenReturn(true);

        assertThatThrownBy(() -> service.importVenueByVenueId(1))
                .isInstanceOf(StadiumConflictException.class)
                .hasMessage("Stadium already exists: Old Trafford");

        when(stadiumRepository.existsByName("Old Trafford")).thenReturn(false);
        when(apiFootballVenueClient.getVenueById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importVenueByVenueId(99))
                .isInstanceOf(StadiumNotFoundException.class)
                .hasMessage("Venue not found for id: 99");
    }

    @Test
    void bulkImportFromLeagueTeamsAndLocalSkipDuplicatesAndInvalidEntries() {
        ApiFootballVenueClient apiFootballVenueClient = mock(ApiFootballVenueClient.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        StadiumBulkImportService service = new StadiumBulkImportService(apiFootballVenueClient, stadiumRepository);
        when(apiFootballVenueClient.getTeamsByLeagueAndSeason(140, 2026))
                .thenReturn(new ApiFootballVenueClient.TeamsResponse(java.util.List.of(
                        new ApiFootballVenueClient.Team(1, "Manchester City", new ApiFootballVenueClient.ExternalVenue(1, "Old Trafford", "Manchester", "England", 74879)),
                        new ApiFootballVenueClient.Team(2, "Liverpool", null),
                        new ApiFootballVenueClient.Team(3, "Spurs", new ApiFootballVenueClient.ExternalVenue(2, "Anfield", "Liverpool", "England", 54074))
                )));
        when(stadiumRepository.existsByName("Old Trafford")).thenReturn(false);
        when(stadiumRepository.existsByName("Anfield")).thenReturn(true);
        when(stadiumRepository.save(any(Stadium.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StadiumBulkImportService.StadiumBulkImportSummary remoteSummary = service.importFromLeagueTeams(140, 2026);
        StadiumBulkImportService.StadiumBulkImportSummary localSummary = service.importFromLocal();

        assertThat(remoteSummary.imported()).isEqualTo(1);
        assertThat(remoteSummary.skipped()).isEqualTo(2);
        assertThat(localSummary.imported()).isGreaterThanOrEqualTo(1);
        assertThat(localSummary.created()).isNotEmpty();
    }

    @Test
    void bulkImportFromLeagueTeamsHandlesBlankVenueNamesAndAllDuplicateLocalData() {
        ApiFootballVenueClient apiFootballVenueClient = mock(ApiFootballVenueClient.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        StadiumBulkImportService service = new StadiumBulkImportService(apiFootballVenueClient, stadiumRepository);
        when(apiFootballVenueClient.getTeamsByLeagueAndSeason(140, 2026))
                .thenReturn(new ApiFootballVenueClient.TeamsResponse(Arrays.asList(
                        null,
                        new ApiFootballVenueClient.Team(1, "Manchester City", new ApiFootballVenueClient.ExternalVenue(1, " ", "Manchester", "England", 74879))
                )));
        when(stadiumRepository.existsByName("Old Trafford")).thenReturn(true);
        when(stadiumRepository.existsByName("Anfield")).thenReturn(true);

        StadiumBulkImportService.StadiumBulkImportSummary remoteSummary = service.importFromLeagueTeams(140, 2026);
        StadiumBulkImportService.StadiumBulkImportSummary localSummary = service.importFromLocal();

        assertThat(remoteSummary.imported()).isZero();
        assertThat(remoteSummary.skipped()).isEqualTo(2);
        assertThat(localSummary.imported()).isZero();
        assertThat(localSummary.skipped()).isEqualTo(2);
    }
}

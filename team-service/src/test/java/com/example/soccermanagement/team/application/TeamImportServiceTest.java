package com.example.soccermanagement.team.application;

import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifies application-layer branches and use-case behavior for the team service.
 */
@ActiveProfiles("testing")
class TeamImportServiceTest {

    @Test
    void importTeamsByCompetitionCodeUsesSeedFallbackWhenCompetitionIsMissing() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        when(repository.existsByName("Arsenal")).thenReturn(false);
        when(repository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        TeamImportSummary summary = service.importTeamsByCompetitionCode("PL");

        assertThat(summary.imported()).isEqualTo(3);
        assertThat(summary.skipped()).isEqualTo(0);
        server.verify();
    }

    @Test
    void importTeamsByCompetitionCodeCallsRemoteApiWhenCompetitionExists() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        when(repository.existsByName("Arsenal")).thenReturn(false);
        when(repository.existsByName("Liverpool")).thenReturn(true);
        when(repository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andRespond(withSuccess("{\"name\":\"Premier League\"}", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9092/competitions/PL/teams"))
                .andRespond(withSuccess("""
                        [
                          {"id":"57","name":"Arsenal"},
                          {"id":"64","name":"Liverpool"},
                          {"id":"99","name":" "}
                        ]
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        TeamImportSummary summary = service.importTeamsByCompetitionCode("PL");

        assertThat(summary.imported()).isEqualTo(1);
        assertThat(summary.skipped()).isEqualTo(2);
        server.verify();
    }

    @Test
    void importFromLocalPreservesStableIdsAndSkipsDuplicates() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        when(repository.existsByName("Manchester City")).thenReturn(false);
        when(repository.existsByName("Liverpool")).thenReturn(true);
        when(repository.existsByName("Leeds United")).thenReturn(false);
        when(repository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamImportSummary summary = service.importFromLocal();

        assertThat(summary.imported()).isEqualTo(2);
        assertThat(summary.skipped()).isEqualTo(1);
    }

    @Test
    void importTeamsByCompetitionCodeFallsBackForMissingNameAndHandles404Teams() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));
        when(repository.existsByName("Manchester City")).thenReturn(true);
        when(repository.existsByName("Liverpool")).thenReturn(true);
        when(repository.existsByName("Leeds United")).thenReturn(true);

        TeamImportSummary fallbackSummary = service.importTeamsByCompetitionCode("PL");

        assertThat(fallbackSummary.imported()).isZero();
        assertThat(fallbackSummary.skipped()).isEqualTo(3);

        TeamImportService secondService = new TeamImportService("http://localhost:9092", repository);
        RestTemplate secondRestTemplate = (RestTemplate) ReflectionTestUtils.getField(secondService, "restTemplate");
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(secondRestTemplate).build();
        secondServer.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andRespond(withSuccess("{\"name\":\"Premier League\"}", org.springframework.http.MediaType.APPLICATION_JSON));
        secondServer.expect(requestTo("http://localhost:9092/competitions/PL/teams"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        TeamImportSummary notFoundSummary = secondService.importTeamsByCompetitionCode("PL");

        assertThat(notFoundSummary.imported()).isZero();
        assertThat(notFoundSummary.skipped()).isZero();
    }

    @Test
    void importTeamsByCompetitionCodePropagatesUnexpectedClientErrorsAndBlankCodeUsesEmptyFallback() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> service.importTeamsByCompetitionCode("PL"))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);

        TeamImportService secondService = new TeamImportService("http://localhost:9092", repository);
        RestTemplate secondRestTemplate = (RestTemplate) ReflectionTestUtils.getField(secondService, "restTemplate");
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(secondRestTemplate).build();
        secondServer.expect(requestTo("http://localhost:9092/competitions/"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        TeamImportSummary blankCodeSummary = secondService.importTeamsByCompetitionCode(" ");

        assertThat(blankCodeSummary.imported()).isZero();
        assertThat(blankCodeSummary.skipped()).isZero();
    }

    @Test
    void importFromLocalReturnsAllSkippedWhenEverythingAlreadyExists() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        when(repository.existsByName("Manchester City")).thenReturn(true);
        when(repository.existsByName("Liverpool")).thenReturn(true);
        when(repository.existsByName("Leeds United")).thenReturn(true);

        TeamImportSummary summary = service.importFromLocal();

        assertThat(summary.imported()).isZero();
        assertThat(summary.skipped()).isEqualTo(3);
    }

    @Test
    void importTeamsByCompetitionCodeHandlesNullTeamBody() {
        TeamRepository repository = mock(TeamRepository.class);
        TeamImportService service = new TeamImportService("http://localhost:9092", repository);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andRespond(withSuccess("{\"name\":\"Premier League\"}", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9092/competitions/PL/teams"))
                .andRespond(withSuccess("null", org.springframework.http.MediaType.APPLICATION_JSON));

        TeamImportSummary summary = service.importTeamsByCompetitionCode("PL");

        assertThat(summary.imported()).isZero();
        assertThat(summary.skipped()).isZero();
    }
}

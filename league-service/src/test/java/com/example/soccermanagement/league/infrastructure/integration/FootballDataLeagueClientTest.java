package com.example.soccermanagement.league.infrastructure.integration;

import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Covers downstream client success and failure handling for the league service.
 */
@ActiveProfiles("testing")
class FootballDataLeagueClientTest {

    @Test
    void getCompetitionByCodeUsesSeedFallbackWhenTokenIsBlank() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://unused", "");

        FootballDataLeagueClient.CompetitionDto competition = client.getCompetitionByCode("PL");

        assertThat(competition.name()).isEqualTo("Premier League");
    }

    @Test
    void getCompetitionByCodeThrowsNotFoundWhenSeedIsMissing() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://unused", "");

        assertThatThrownBy(() -> client.getCompetitionByCode("UNKNOWN"))
                .isInstanceOf(LeagueNotFoundException.class)
                .hasMessage("External competition not found for code: UNKNOWN");
    }

    @Test
    void getCompetitionByCodeCallsRemoteApiWhenTokenExists() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9091", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9091/competitions/PL"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Auth-Token", "token-123"))
                .andRespond(withSuccess("{\"name\":\"Premier League\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        FootballDataLeagueClient.CompetitionDto competition = client.getCompetitionByCode("PL");

        assertThat(competition.name()).isEqualTo("Premier League");
        server.verify();
    }

    @Test
    void getCompetitionByCodeMapsRateLimitResponse() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9091", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9091/competitions/PL"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.getCompetitionByCode("PL"))
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("External API rate limit exceeded");
    }

    @Test
    void getCompetitionByCodeMapsRemoteNotFoundWhenTokenExists() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9091", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9091/competitions/UNKNOWN"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getCompetitionByCode("UNKNOWN"))
                .isInstanceOf(LeagueNotFoundException.class)
                .hasMessage("External competition not found for code: UNKNOWN");
    }
}

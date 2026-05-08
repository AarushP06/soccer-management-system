package com.example.soccermanagement.team.infrastructure.integration;

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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Covers downstream client success and failure handling for the team service.
 */
@ActiveProfiles("testing")
class FootballDataLeagueClientTest {

    @Test
    void getTeamsByCompetitionCodeReturnsEmptyOnNotFoundAndPropagatesOtherClientErrors() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9092", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL/teams"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Auth-Token", "token-123"))
                .andRespond(withSuccess("""
                        [{"id":"57","name":"Arsenal"},{"id":"64","name":"Liverpool"}]
                        """, org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9092/competitions/UNKNOWN/teams"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://localhost:9092/competitions/RATE/teams"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThat(client.getTeamsByCompetitionCode("PL")).hasSize(2);
        assertThat(client.getTeamsByCompetitionCode("UNKNOWN")).isEmpty();
        assertThatThrownBy(() -> client.getTeamsByCompetitionCode("RATE"))
                .isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    @Test
    void getCompetitionByCodeReturnsNullOnNotFound() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9092", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"name":"Premier League"}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9092/competitions/UNKNOWN"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getCompetitionByCode("PL").name()).isEqualTo("Premier League");
        assertThat(client.getCompetitionByCode("UNKNOWN")).isNull();
    }

    @Test
    void getCompetitionByCodeMapsRateLimitResponses() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9092", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9092/competitions/RATE"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.getCompetitionByCode("RATE"))
                .isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    @Test
    void getTeamsAndCompetitionReturnEmptyOrNullWhenBodiesAreMissing() {
        FootballDataLeagueClient client = new FootballDataLeagueClient("http://localhost:9092", "token-123");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9092/competitions/PL/teams"))
                .andRespond(withSuccess("null", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9092/competitions/PL"))
                .andRespond(withSuccess("null", org.springframework.http.MediaType.APPLICATION_JSON));

        assertThat(client.getTeamsByCompetitionCode("PL")).isEmpty();
        assertThat(client.getCompetitionByCode("PL")).isNull();
    }
}

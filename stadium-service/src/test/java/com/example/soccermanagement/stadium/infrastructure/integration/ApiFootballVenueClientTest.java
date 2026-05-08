package com.example.soccermanagement.stadium.infrastructure.integration;

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
 * Covers downstream client success and failure handling for the stadium service.
 */
@ActiveProfiles("testing")
class ApiFootballVenueClientTest {

    @Test
    void venueClientUsesLocalSeedsWhenApiKeyIsMissing() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://unused", "");

        assertThat(client.getTeamsByLeagueAndSeason(140, 2026).response()).hasSize(2);
        assertThatThrownBy(() -> client.getVenueById(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to load seed resource: data/venues.json");
    }

    @Test
    void venueClientCallsRemoteApiWhenApiKeyExists() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://localhost:9093", "api-key");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9093/venues?id=1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-apisports-key", "api-key"))
                .andRespond(withSuccess("""
                        {"response":[{"id":1,"name":"Old Trafford","city":"Manchester","country":"England","capacity":74879}]}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        server.expect(requestTo("http://localhost:9093/teams?league=140&season=2026"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"response":[{"id":1,"name":"Manchester City","venue":{"id":1,"name":"Old Trafford","city":"Manchester","country":"England","capacity":74879}}]}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        assertThat(client.getVenueById(1)).isPresent();
        assertThat(client.getTeamsByLeagueAndSeason(140, 2026).response()).hasSize(1);
        server.verify();
    }

    @Test
    void venueClientMapsRateLimitResponses() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://localhost:9093", "api-key");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://localhost:9093/venues?id=1"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.getVenueById(1))
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("External API rate limit exceeded");
    }

    @Test
    void venueClientReturnsSeedOrEmptyResponsesForFallbackPaths() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://unused", "");

        assertThat(client.getTeamsByLeagueAndSeason(null, 2026).response()).isEmpty();
        assertThat(client.getTeamsByLeagueAndSeason(999, 2026).response()).isEmpty();
        assertThatThrownBy(() -> client.getVenueById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to load seed resource: data/venues.json");
    }

    @Test
    void venueClientReturnsEmptyFor404AndNonRateLimitErrors() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://localhost:9093", "api-key");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9093/venues?id=404"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://localhost:9093/venues?id=500"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        server.expect(requestTo("http://localhost:9093/teams?league=140&season=2026"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        server.expect(requestTo("http://localhost:9093/teams?league=140&season=2027"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThat(client.getVenueById(404)).isEmpty();
        assertThat(client.getVenueById(500)).isEmpty();
        assertThat(client.getTeamsByLeagueAndSeason(140, 2026).response()).isEmpty();
        assertThatThrownBy(() -> client.getTeamsByLeagueAndSeason(140, 2027))
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("External API rate limit exceeded");
    }

    @Test
    void venueClientReturnsEmptyWhenRemoteBodiesOrResponsesAreNull() {
        ApiFootballVenueClient client = new ApiFootballVenueClient("http://localhost:9093", "api-key");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        server.expect(requestTo("http://localhost:9093/venues?id=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9093/venues?id=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"response\":null}", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9093/teams?league=140&season=2026"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:9093/teams?league=140&season=2028"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"response\":null}", org.springframework.http.MediaType.APPLICATION_JSON));

        assertThat(client.getVenueById(1)).isEmpty();
        assertThat(client.getVenueById(2)).isEmpty();
        assertThat(client.getTeamsByLeagueAndSeason(140, 2026).response()).isEmpty();
        assertThat(client.getTeamsByLeagueAndSeason(140, 2028).response()).isEmpty();
    }
}

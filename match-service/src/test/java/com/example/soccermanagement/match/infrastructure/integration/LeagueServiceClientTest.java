package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Covers downstream client success and failure handling for the match service.
 */
@RestClientTest(LeagueServiceClient.class)
@ActiveProfiles("testing")
class LeagueServiceClientTest {

    @Autowired
    private LeagueServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findNameByIdReturnsEmptyOn404() {
        UUID internalId = LeagueReferenceMapper.toInternalUuid(100L);
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findNameById(internalId)).isEmpty();
    }

    @Test
    void existsByIdWraps500AsExternalServiceException() {
        UUID internalId = LeagueReferenceMapper.toInternalUuid(100L);
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.existsById(internalId))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("League service unavailable");
    }

    @Test
    void findByNameMapsLeagueFromSupportingService() {
        server.expect(once(), requestTo("http://localhost:8082/api/leagues"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":\"100\",\"name\":\"Premier League\",\"externalCode\":\"PL\"}]", MediaType.APPLICATION_JSON));

        var league = client.findByName("Premier League");

        assertThat(league).isPresent();
        assertThat(league.orElseThrow().name()).isEqualTo("Premier League");
    }

    @Test
    void existsByIdAndExternalCodeReturnExpectedValues() {
        UUID internalId = LeagueReferenceMapper.toInternalUuid(100L);
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":\"100\",\"name\":\"Premier League\",\"externalCode\":\"PL\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":\"100\",\"name\":\"Premier League\",\"externalCode\":\"PL\"}", MediaType.APPLICATION_JSON));

        assertThat(client.existsById(internalId)).isTrue();
        assertThat(client.findExternalCodeById(internalId)).contains("PL");
    }

    @Test
    void invalidOrUnparseableLeagueReferencesReturnEmptyOrFalse() {
        UUID invalid = UUID.randomUUID();

        assertThat(client.existsById(invalid)).isFalse();
        assertThat(client.findNameById(invalid)).isEmpty();
        assertThat(client.findExternalCodeById(invalid)).isEmpty();

        server.expect(once(), requestTo("http://localhost:8082/api/leagues"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":\"bad\",\"name\":\"Premier League\",\"externalCode\":\"PL\"}]", MediaType.APPLICATION_JSON));

        assertThat(client.findByName("Premier League")).isEmpty();
    }

    @Test
    void findByNameWrapsServerErrors() {
        server.expect(once(), requestTo("http://localhost:8082/api/leagues"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findByName("Premier League"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("League service unavailable");
    }

    @Test
    void findNameAndExternalCodeReturnEmptyWhenBodyIsNullOrNotFound() {
        UUID internalId = LeagueReferenceMapper.toInternalUuid(100L);
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8082/api/leagues/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findNameById(internalId)).isEmpty();
        assertThat(client.findExternalCodeById(internalId)).isEmpty();
    }
}

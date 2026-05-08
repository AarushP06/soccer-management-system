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
@RestClientTest(TeamServiceClient.class)
@ActiveProfiles("testing")
class TeamServiceClientTest {

    @Autowired
    private TeamServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void existsByIdReturnsFalseOn404() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.existsById(id)).isFalse();
    }

    @Test
    void findNameByIdWraps500AsExternalServiceException() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findNameById(id))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Team service unavailable");
    }

    @Test
    void findNameAndExternalIdReturnValuesWhenTeamExists() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":"%s","name":"Arsenal","externalId":"57"}
                        """.formatted(id), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":"%s","name":"Arsenal","externalId":"57"}
                        """.formatted(id), MediaType.APPLICATION_JSON));

        assertThat(client.findNameById(id)).contains("Arsenal");
        assertThat(client.findExternalIdById(id)).contains("57");
    }

    @Test
    void findByNameReturnsEmptyWhenRemotePayloadIsMissing() {
        server.expect(once(), requestTo("http://localhost:8083/api/teams?name=Arsenal"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.findByName("Arsenal")).isEmpty();
    }

    @Test
    void existsByIdReturnsFalseOnBadRequestAndFindByNameWrapsServerError() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        server.expect(once(), requestTo("http://localhost:8083/api/teams?name=Arsenal"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThat(client.existsById(id)).isFalse();
        assertThatThrownBy(() -> client.findByName("Arsenal"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Team service unavailable");
    }

    @Test
    void findNameAndExternalIdReturnEmptyWhenBodyIsNullOrNotFound() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8083/api/teams/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findNameById(id)).isEmpty();
        assertThat(client.findExternalIdById(id)).isEmpty();
    }
}

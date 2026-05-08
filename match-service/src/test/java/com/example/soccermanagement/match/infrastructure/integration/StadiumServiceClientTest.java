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
@RestClientTest(StadiumServiceClient.class)
@ActiveProfiles("testing")
class StadiumServiceClientTest {

    @Autowired
    private StadiumServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findNameByIdReturnsEmptyOn404() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findNameById(id)).isEmpty();
    }

    @Test
    void findExternalVenueIdByIdWraps500AsExternalServiceException() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findExternalVenueIdById(id))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Stadium service unavailable");
    }

    @Test
    void existsByIdAndVenueMetadataReturnExpectedValues() {
        UUID id = UUID.randomUUID();
        String payload = """
                {"id":"%s","name":"Old Trafford","externalVenueId":1}
                """.formatted(id);
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThat(client.existsById(id)).isTrue();
        assertThat(client.findExternalVenueIdById(id)).contains(1);
    }

    @Test
    void existsByIdReturnsFalseOnBadRequestAndNameWrapsServerError() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThat(client.existsById(id)).isFalse();
        assertThatThrownBy(() -> client.findNameById(id))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Stadium service unavailable");
    }

    @Test
    void findNameAndExternalVenueReturnEmptyWhenBodyIsNullOrNotFound() {
        UUID id = UUID.randomUUID();
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://localhost:8084/api/stadiums/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findNameById(id)).isEmpty();
        assertThat(client.findExternalVenueIdById(id)).isEmpty();
    }
}

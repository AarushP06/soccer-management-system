package com.example.soccermanagement.match.infrastructure.integration;

import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers downstream client success and failure handling for the match service.
 */
@ActiveProfiles("testing")
class FootballDataMatchClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void clientUsesSeedFallbackWhenTokenIsBlank() {
        FootballDataMatchClient client = new FootballDataMatchClient("http://unused", "");

        assertThat(client.getCompetitionByCode("PL").name()).isEqualTo("Premier League");
        assertThat(client.getMatchesByCompetitionCode("PL")).hasSize(1);
        assertThat(client.getMatchesByCompetitionCode("UNKNOWN")).isEmpty();
    }

    @Test
    void clientCallsRemoteApiAndHandlesNotFound() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/competitions/PL", exchange -> writeJson(exchange, 200, "{\"name\":\"Premier League\"}"));
        server.createContext("/competitions/PL/matches", exchange -> writeJson(exchange, 200, "{\"matches\":[{\"id\":\"m1\",\"homeTeam\":{\"id\":\"57\",\"name\":\"Arsenal\"},\"awayTeam\":{\"id\":\"64\",\"name\":\"Liverpool\"}}]}"));
        server.createContext("/competitions/UNKNOWN", exchange -> writeJson(exchange, 404, "{}"));
        server.createContext("/competitions/UNKNOWN/matches", exchange -> writeJson(exchange, 404, "{}"));
        server.start();

        FootballDataMatchClient client = new FootballDataMatchClient("http://localhost:" + server.getAddress().getPort(), "token");

        assertThat(client.getCompetitionByCode("PL").name()).isEqualTo("Premier League");
        List<FootballDataMatchClient.ExternalMatch> matches = client.getMatchesByCompetitionCode("PL");
        assertThat(matches).hasSize(1);
        assertThat(client.getCompetitionByCode("UNKNOWN")).isNull();
        assertThat(client.getMatchesByCompetitionCode("UNKNOWN")).isEmpty();
    }

    @Test
    void clientMapsRateLimitResponses() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/competitions/PL", exchange -> writeJson(exchange, 429, "{}"));
        server.start();

        FootballDataMatchClient client = new FootballDataMatchClient("http://localhost:" + server.getAddress().getPort(), "token");

        assertThatThrownBy(() -> client.getCompetitionByCode("PL"))
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("External API rate limit exceeded");
    }

    @Test
    void clientMapsMatchRateLimitResponses() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/competitions/PL/matches", exchange -> writeJson(exchange, 429, "{}"));
        server.start();

        FootballDataMatchClient client = new FootballDataMatchClient("http://localhost:" + server.getAddress().getPort(), "token");

        assertThatThrownBy(() -> client.getMatchesByCompetitionCode("PL"))
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("External API rate limit exceeded");
    }

    @Test
    void clientReturnsEmptyForNullRemoteBodies() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/competitions/PL", exchange -> writeJson(exchange, 200, "null"));
        server.createContext("/competitions/PL/matches", exchange -> writeJson(exchange, 200, "{}"));
        server.start();

        FootballDataMatchClient client = new FootballDataMatchClient("http://localhost:" + server.getAddress().getPort(), "token");

        assertThat(client.getCompetitionByCode("PL")).isNull();
        assertThat(client.getMatchesByCompetitionCode("PL")).isEmpty();
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }
}

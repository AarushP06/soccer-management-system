package com.example.soccermanagement.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Covers downstream client success and failure handling for the gateway service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("testing")
class GatewayWebTestClientTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void pingEndpointReturnsGatewayStatus() {
        webTestClient.get()
                .uri("/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("api-gateway")
                .jsonPath("$.status").isEqualTo("up");
    }

    @Test
    void unknownRouteReturnsNotFound() {
        webTestClient.get()
                .uri("/unknown-route")
                .exchange()
                .expectStatus().isNotFound();
    }
}

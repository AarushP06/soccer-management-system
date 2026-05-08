package com.example.soccermanagement.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests expected behavior and edge cases in the gateway service.
 */
@SpringBootTest
@ActiveProfiles("testing")
class RouteLocatorConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void routeLocatorContainsExpectedRouteIdsAndUris() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();

        assertThat(routes).isNotNull();

        Map<String, String> routeUris = routes.stream()
                .collect(Collectors.toMap(Route::getId, route -> route.getUri().toString(), (left, right) -> left));

        assertThat(routeUris)
                .containsEntry("match-service", "http://localhost:8081")
                .containsEntry("league-service", "http://localhost:8082")
                .containsEntry("team-service", "http://localhost:8083")
                .containsEntry("stadium-service", "http://localhost:8084");
    }
}

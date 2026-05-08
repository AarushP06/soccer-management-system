package com.example.soccermanagement.gateway;

import com.example.soccermanagement.gateway.api.AdviceController;
import com.example.soccermanagement.gateway.application.exception.GatewayRouteException;
import com.example.soccermanagement.gateway.application.exception.GatewayServiceUnavailableException;
import com.example.soccermanagement.gateway.domain.exception.GatewayConfigurationException;
import com.example.soccermanagement.gateway.domain.exception.GatewayDomainException;
import com.example.soccermanagement.shared.config.DotenvEnvironmentPostProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers support classes and small branches that are hard to reach through higher-level tests.
 */
@ActiveProfiles("testing")
class GatewaySupportCoverageTest {

    private static final Path DOTENV_PATH = Path.of(".env");

    @AfterEach
    void cleanupDotenv() throws Exception {
        Files.deleteIfExists(DOTENV_PATH);
    }

    @Test
    void pingReturnsGatewayStatus() {
        Map<String, String> response = new PingController().ping().block();

        assertThat(response)
                .containsEntry("service", "api-gateway")
                .containsEntry("status", "up");
    }

    @Test
    void adviceControllerMapsExpectedStatuses() {
        AdviceController adviceController = new AdviceController();

        assertThat(adviceController.handleServiceUnavailable(new GatewayServiceUnavailableException("downstream down")).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(adviceController.handleRouteException(new GatewayRouteException("bad route")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(adviceController.handleDomainExceptions(new GatewayConfigurationException("bad config")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(adviceController.handleDomainExceptions(new GatewayDomainException("bad domain")).getBody())
                .isEqualTo("bad domain");
    }

    @Test
    void dotenvEnvironmentPostProcessorLoadsValuesFromEnvFile() throws Exception {
        Files.writeString(DOTENV_PATH, """
                # comment
                API_KEY=test-key
                QUOTED_VALUE="quoted"
                IGNORED_LINE
                """);

        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(GatewayApplication.class));

        assertThat(environment.getProperty("API_KEY")).isEqualTo("test-key");
        assertThat(environment.getProperty("QUOTED_VALUE")).isEqualTo("quoted");
        assertThat(new DotenvEnvironmentPostProcessor().getOrder()).isEqualTo(Integer.MIN_VALUE);
    }
}

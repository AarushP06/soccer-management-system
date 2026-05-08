package com.example.soccermanagement.league;

import com.example.soccermanagement.league.api.AdviceController;
import com.example.soccermanagement.league.api.TestController;
import com.example.soccermanagement.league.application.exception.LeagueImportException;
import com.example.soccermanagement.league.application.exception.LeagueServiceException;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import com.example.soccermanagement.shared.config.DotenvEnvironmentPostProcessor;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.example.soccermanagement.shared.infrastructure.seed.SeedResourceLoader;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers support classes and small branches that are hard to reach through higher-level tests.
 */
@ActiveProfiles("testing")
class LeagueSupportCoverageTest {

    private static final Path DOTENV_PATH = Path.of(".env");

    @AfterEach
    void cleanupDotenv() throws Exception {
        Files.deleteIfExists(DOTENV_PATH);
    }

    @Test
    void pingReturnsLeagueServiceStatus() {
        Map<String, String> response = new PingController().ping();

        assertThat(response)
                .containsEntry("service", "league-service")
                .containsEntry("status", "up");
    }

    @Test
    void adviceControllerMapsExpectedStatuses() {
        AdviceController adviceController = new AdviceController();

        assertThat(adviceController.handleNotFound(new LeagueNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(adviceController.handleConflict(new LeagueConflictException("duplicate")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(adviceController.handleServiceErrors(new LeagueImportException("import failed")).getBody())
                .isEqualTo("import failed");
        assertThat(adviceController.handleServiceErrors(new LeagueServiceException("service failed")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void dotenvAndSeedLoaderUtilitiesWork() throws Exception {
        Files.writeString(DOTENV_PATH, "LEAGUE_TOKEN='league-test'");
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("LEAGUE_TOKEN")).isEqualTo("league-test");

        LeagueSeed[] seeds = SeedResourceLoader.load("data/leagues.json", LeagueSeed[].class);
        assertThat(seeds).hasSize(2);
        assertThat(seeds[0].name).isEqualTo("Premier League");
        assertThat(new DotenvEnvironmentPostProcessor().getOrder()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void dotenvIgnoresCommentsAndInvalidAssignments() throws Exception {
        Files.writeString(DOTENV_PATH, """
                # comment
                INVALID
                LEAGUE_TOKEN="quoted"
                EMPTY=
                """);
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("LEAGUE_TOKEN")).isEqualTo("quoted");
        assertThat(environment.getProperty("EMPTY")).isEmpty();
        assertThat(environment.getProperty("INVALID")).isNull();
    }

    @Test
    void testControllerThrowsSimulatedRateLimit() {
        assertThatThrownBy(() -> new TestController().simulateRateLimit())
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("Simulated rate limit");
    }

    private static class LeagueSeed {
        public Long id;
        public String name;
    }
}

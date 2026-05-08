package com.example.soccermanagement.match;

import com.example.soccermanagement.match.api.AdviceController;
import com.example.soccermanagement.match.api.TestController;
import com.example.soccermanagement.match.api.mapper.MatchApiMapper;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchImportException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException;
import com.example.soccermanagement.match.domain.Match;
import com.example.soccermanagement.match.domain.exception.MatchDomainException;
import com.example.soccermanagement.match.domain.exception.MatchValidationException;
import com.example.soccermanagement.shared.config.DotenvEnvironmentPostProcessor;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.example.soccermanagement.shared.infrastructure.seed.SeedResourceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers support classes and small branches that are hard to reach through higher-level tests.
 */
@ActiveProfiles("testing")
class MatchSupportCoverageTest {

    private static final Path DOTENV_PATH = Path.of(".env");

    @AfterEach
    void cleanupDotenv() throws Exception {
        Files.deleteIfExists(DOTENV_PATH);
    }

    @Test
    void pingReturnsMatchServiceStatus() {
        Map<String, String> response = new PingController().ping();

        assertThat(response)
                .containsEntry("service", "match-service")
                .containsEntry("status", "up");
    }

    @Test
    void adviceAndExceptionsMapExpectedStatuses() {
        AdviceController adviceController = new AdviceController();

        assertThat(adviceController.handleNotFound(new MatchNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(adviceController.handleConflict(new MatchConflictException("duplicate")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(adviceController.handleImportErrors(new MatchImportException("import failed")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(adviceController.handleImportErrors(
                new MatchImportException("Failed to import matches: Stadium not found: " + UUID.randomUUID(),
                        new MatchImportException("Stadium not found: " + UUID.randomUUID())))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(adviceController.handleExternalService(new ExternalServiceException("league down")).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(adviceController.handleRelatedNotFound(new RelatedEntityNotFoundException("league missing")).getBody())
                .isEqualTo("league missing");
        assertThat(adviceController.handleValidation(new MatchValidationException("bad match")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(adviceController.handleInvalidInput(new IllegalArgumentException("bad uuid")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(adviceController.handleInvalidInput(new MethodArgumentTypeMismatchException(
                "bad-uuid", UUID.class, "stadiumId", null, new IllegalArgumentException("bad uuid"))).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(new MatchDomainException("domain issue").getMessage()).isEqualTo("domain issue");
    }

    @Test
    void dotenvAndSeedLoaderUtilitiesWork() throws Exception {
        Files.writeString(DOTENV_PATH, "MATCH_TOKEN=match-test");
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("MATCH_TOKEN")).isEqualTo("match-test");

        MatchSeed[] seeds = SeedResourceLoader.load("data/matches.json", MatchSeed[].class);
        assertThat(seeds).hasSize(1);
        assertThat(seeds[0].leagueName).isEqualTo("Premier League");
    }

    @Test
    void dotenvIgnoresCommentsInvalidLinesAndMissingFiles() throws Exception {
        Files.writeString(DOTENV_PATH, """
                # comment
                INVALID
                QUOTED="value"
                SINGLE='quoted'
                EMPTY=
                """);
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("QUOTED")).isEqualTo("value");
        assertThat(environment.getProperty("SINGLE")).isEqualTo("quoted");
        assertThat(environment.getProperty("EMPTY")).isEmpty();

        Files.deleteIfExists(DOTENV_PATH);
        ConfigurableEnvironment secondEnvironment = new StandardEnvironment();
        new DotenvEnvironmentPostProcessor().postProcessEnvironment(secondEnvironment, new SpringApplication(PingController.class));
        assertThat(secondEnvironment.getProperty("QUOTED")).isNull();
    }

    @Test
    void mapperAndTestControllerExposeExpectedSupportBehavior() {
        Match match = Match.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var response = MatchApiMapper.toResponse(match, "ext-1", "Premier League", "PL", "Arsenal", "57", "Liverpool", "64", "Old Trafford", 1);

        assertThat(response.externalMatchId()).isEqualTo("ext-1");
        assertThat(response.leagueName()).isEqualTo("Premier League");
        assertThatThrownBy(() -> new TestController().simulateRateLimit())
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("Simulated rate limit");
    }

    private static class MatchSeed {
        public String id;
        public Integer leagueId;
        public String leagueName;
        public String homeTeamId;
        public String homeTeamName;
        public String awayTeamId;
        public String awayTeamName;
        public String stadiumId;
        public String externalId;
        public String status;
    }
}

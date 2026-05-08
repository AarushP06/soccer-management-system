package com.example.soccermanagement.stadium;

import com.example.soccermanagement.shared.config.DotenvEnvironmentPostProcessor;
import com.example.soccermanagement.shared.infrastructure.seed.SeedResourceLoader;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.example.soccermanagement.stadium.api.AdviceController;
import com.example.soccermanagement.stadium.api.TestController;
import com.example.soccermanagement.stadium.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.stadium.application.exception.StadiumConflictException;
import com.example.soccermanagement.stadium.application.exception.StadiumImportException;
import com.example.soccermanagement.stadium.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.stadium.domain.Stadium;
import com.example.soccermanagement.stadium.domain.exception.DomainException;
import com.example.soccermanagement.stadium.domain.exception.StadiumValidationException;
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
class StadiumSupportCoverageTest {

    private static final Path DOTENV_PATH = Path.of(".env");

    @AfterEach
    void cleanupDotenv() throws Exception {
        Files.deleteIfExists(DOTENV_PATH);
    }

    @Test
    void pingReturnsStadiumServiceStatus() {
        Map<String, String> response = new PingController().ping();

        assertThat(response)
                .containsEntry("service", "stadium-service")
                .containsEntry("status", "up");
    }

    @Test
    void adviceAndDomainExceptionsMapExpectedStatuses() {
        AdviceController adviceController = new AdviceController();

        assertThat(adviceController.handleNotFound(new StadiumNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(adviceController.handleConflict(new StadiumConflictException("duplicate")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(adviceController.handleImportErrors(new StadiumImportException("import failed")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(adviceController.handleDomain(new DomainException("invalid")).getBody()).isEqualTo("invalid");
        assertThat(new StadiumValidationException("bad stadium").getMessage()).isEqualTo("bad stadium");
    }

    @Test
    void dotenvAndSeedLoaderUtilitiesWork() throws Exception {
        Files.writeString(DOTENV_PATH, "STADIUM_TOKEN=stadium-test");
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("STADIUM_TOKEN")).isEqualTo("stadium-test");

        StadiumSeed[] seeds = SeedResourceLoader.load("data/stadiums.json", StadiumSeed[].class);
        assertThat(seeds).hasSize(2);
        assertThat(seeds[0].name).isEqualTo("Old Trafford");
    }

    @Test
    void dotenvIgnoresCommentsAndInvalidAssignments() throws Exception {
        Files.writeString(DOTENV_PATH, """
                # comment
                INVALID
                STADIUM_TOKEN='quoted'
                EMPTY=
                """);
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("STADIUM_TOKEN")).isEqualTo("quoted");
        assertThat(environment.getProperty("EMPTY")).isEmpty();
        assertThat(environment.getProperty("INVALID")).isNull();
    }

    @Test
    void mapperAndTestControllerExposeExpectedSupportBehavior() {
        Stadium stadium = Stadium.createFromExternal("Old Trafford", 1, "Manchester", "England", 74879);
        assertThat(StadiumApiMapper.toResponse(stadium).city()).isEqualTo("Manchester");

        assertThatThrownBy(() -> new TestController().simulateRateLimit())
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("Simulated rate limit");
    }

    private static class StadiumSeed {
        public String id;
        public String name;
        public Integer externalVenueId;
        public String city;
        public String country;
        public Integer capacity;
    }
}

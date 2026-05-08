package com.example.soccermanagement.team;

import com.example.soccermanagement.shared.config.DotenvEnvironmentPostProcessor;
import com.example.soccermanagement.shared.infrastructure.seed.SeedResourceLoader;
import com.example.soccermanagement.shared.exception.ExternalApiRateLimitException;
import com.example.soccermanagement.team.api.AdviceController;
import com.example.soccermanagement.team.api.TestController;
import com.example.soccermanagement.team.api.dto.TeamBulkRequest;
import com.example.soccermanagement.team.api.mapper.TeamApiMapper;
import com.example.soccermanagement.team.application.exception.TeamConflictException;
import com.example.soccermanagement.team.application.exception.TeamImportLeagueNotFoundException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import com.example.soccermanagement.team.domain.Team;
import com.example.soccermanagement.team.domain.exception.DomainException;
import com.example.soccermanagement.team.domain.exception.TeamValidationException;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers support classes and small branches that are hard to reach through higher-level tests.
 */
@ActiveProfiles("testing")
class TeamSupportCoverageTest {

    private static final Path DOTENV_PATH = Path.of(".env");

    @AfterEach
    void cleanupDotenv() throws Exception {
        Files.deleteIfExists(DOTENV_PATH);
    }

    @Test
    void pingReturnsTeamServiceStatus() {
        Map<String, String> response = new PingController().ping();

        assertThat(response)
                .containsEntry("service", "team-service")
                .containsEntry("status", "up");
    }

    @Test
    void adviceAndDomainExceptionsMapExpectedStatuses() {
        AdviceController adviceController = new AdviceController();

        assertThat(adviceController.handleNotFound(new TeamNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(adviceController.handleConflict(new TeamConflictException("duplicate")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(adviceController.handleDomain(new DomainException("invalid")).getBody()).isEqualTo("invalid");
        assertThat(new TeamValidationException("bad team").getMessage()).isEqualTo("bad team");
        assertThat(new TeamImportLeagueNotFoundException("missing league").getMessage()).isEqualTo("missing league");
    }

    @Test
    void dotenvAndSeedLoaderUtilitiesWork() throws Exception {
        Files.writeString(DOTENV_PATH, "TEAM_TOKEN=\"team-test\"");
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("TEAM_TOKEN")).isEqualTo("team-test");

        TeamSeed[] seeds = SeedResourceLoader.load("data/teams.json", TeamSeed[].class);
        assertThat(seeds).hasSize(3);
        assertThat(seeds[0].name).isEqualTo("Manchester City");
    }

    @Test
    void dotenvIgnoresCommentsAndInvalidAssignments() throws Exception {
        Files.writeString(DOTENV_PATH, """
                # comment
                INVALID
                TEAM_TOKEN='quoted'
                EMPTY=
                """);
        ConfigurableEnvironment environment = new StandardEnvironment();

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(PingController.class));

        assertThat(environment.getProperty("TEAM_TOKEN")).isEqualTo("quoted");
        assertThat(environment.getProperty("EMPTY")).isEmpty();
        assertThat(environment.getProperty("INVALID")).isNull();
    }

    @Test
    void mapperBulkRequestAndTestControllerAreCovered() {
        Team team = Team.createFromExternal("Arsenal", "57");
        assertThat(TeamApiMapper.toResponse(team).externalId()).isEqualTo("57");

        TeamBulkRequest request = new TeamBulkRequest(team.getId(), "Arsenal");
        assertThat(request.id()).isEqualTo(team.getId());
        assertThat(request.name()).isEqualTo("Arsenal");

        assertThatThrownBy(() -> new TestController().simulateRateLimit())
                .isInstanceOf(ExternalApiRateLimitException.class)
                .hasMessage("Simulated rate limit");
    }

    @Test
    void applicationMainDelegatesToSpringApplication() {
        assertThatCode(() -> TeamServiceApplication.main(new String[]{
                "--spring.profiles.active=testing",
                "--spring.main.web-application-type=none",
                "--spring.main.banner-mode=off"
        })).doesNotThrowAnyException();
    }

    private static class TeamSeed {
        public String id;
        public String name;
        public String externalId;
    }
}

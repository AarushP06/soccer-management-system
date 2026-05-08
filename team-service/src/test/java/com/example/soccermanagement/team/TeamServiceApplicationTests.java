package com.example.soccermanagement.team;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring Boot context for the team service starts successfully.
 */
@SpringBootTest
@ActiveProfiles("testing")
class TeamServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

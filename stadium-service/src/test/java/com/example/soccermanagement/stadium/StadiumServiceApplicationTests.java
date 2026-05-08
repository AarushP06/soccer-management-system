package com.example.soccermanagement.stadium;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring Boot context for the stadium service starts successfully.
 */
@SpringBootTest
@ActiveProfiles("testing")
class StadiumServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

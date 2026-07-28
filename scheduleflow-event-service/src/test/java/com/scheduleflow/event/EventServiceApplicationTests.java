package com.scheduleflow.event;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the Spring application context loads without errors.
 */
@SpringBootTest
@ActiveProfiles("test")
class EventServiceApplicationTests {

    @Test
    void contextLoads() {
        // Spring context must load without exception for this test to pass
    }
}

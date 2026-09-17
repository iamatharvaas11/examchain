package com.examchain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ExamchainApplication.class)
@ActiveProfiles("test")
class ExamchainApplicationTests {

    @Test
    void contextLoads() {
        // Confirms Spring Boot application context initializes properly with test configuration
    }
}

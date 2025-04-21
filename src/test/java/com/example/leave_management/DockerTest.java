package com.example.leave_management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class DockerTest {

    @Autowired
    private Environment environment;

    @Test
    void verifyDatabaseConfiguration() {
        String dbUrl = environment.getProperty("spring.datasource.url");
        String dbUsername = environment.getProperty("spring.datasource.username");
        String dbPassword = environment.getProperty("spring.datasource.password");

        assertNotNull(dbUrl);
        assertNotNull(dbUsername);
        assertNotNull(dbPassword);

        assertEquals("jdbc:postgresql://postgres:5432/leave_management", dbUrl);
        assertEquals("postgres", dbUsername);
        assertEquals("postgres", dbPassword);
    }

    @Test
    void verifyApplicationPort() {
        String serverPort = environment.getProperty("server.port");
        assertNotNull(serverPort);
        assertEquals("5500", serverPort);
    }

    @Test
    void verifyJpaConfiguration() {
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertNotNull(ddlAuto);
        assertEquals("update", ddlAuto);
    }
}
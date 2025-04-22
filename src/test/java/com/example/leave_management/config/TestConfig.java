package com.example.leave_management.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.mockito.Mockito;

@TestConfiguration
public class TestConfig {

    @Bean
    public SimpMessagingTemplate messagingTemplate() {
        return Mockito.mock(SimpMessagingTemplate.class);
    }
}
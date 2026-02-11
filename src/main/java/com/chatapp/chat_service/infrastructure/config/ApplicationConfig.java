package com.chatapp.chat_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IdGenerator generator() {
        return UUID::randomUUID;
    }
}

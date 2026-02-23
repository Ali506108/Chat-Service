package com.chatapp.chat_service.api.api;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DisplayName("Group controller testing")
public class GroupControllerTest {

    @Container
    static CassandraContainer<?> cassandra = new CassandraContainer<>("chat-scylladb");


    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.cassandra.contact-points", cassandra::getContactPoint);
        registry.add("spring.data.cassandra.port", cassandra::getExposedPorts);
        registry.add("spring.data.cassandra.keyspace-name", () -> "chat_keyspace");
        registry.add("spring.data.cassandra.schema-action", () -> "CREATE_IF_NOT_EXISTS");
        registry.add("spring.data.cassandra.local-datacenter", () -> "datacenter1");
    }



}
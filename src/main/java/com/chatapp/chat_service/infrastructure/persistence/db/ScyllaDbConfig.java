package com.chatapp.chat_service.infrastructure.persistence.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

@Configuration
@EnableReactiveCassandraRepositories(basePackages = "com.chatapp.chat_service.domain.repository")
public class ScyllaDbConfig extends AbstractReactiveCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keySpace;

    @Value("${spring.cassandra.contact-points}")
    private String host;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String datacenter;

    @Override
    protected String getKeyspaceName() {
        return keySpace;
    }

    @Override
    protected String getContactPoints() {
        return host;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return datacenter;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.NONE;
    }
}

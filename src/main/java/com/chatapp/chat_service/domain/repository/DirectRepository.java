package com.chatapp.chat_service.domain.repository;

import com.chatapp.chat_service.domain.model.Direct;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DirectRepository extends ReactiveCassandraRepository<Direct , UUID> {
}

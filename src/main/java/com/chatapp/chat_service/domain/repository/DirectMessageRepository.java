package com.chatapp.chat_service.domain.repository;

import com.chatapp.chat_service.domain.model.DirectMessage;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DirectMessageRepository extends ReactiveCassandraRepository<DirectMessage , UUID> {
}

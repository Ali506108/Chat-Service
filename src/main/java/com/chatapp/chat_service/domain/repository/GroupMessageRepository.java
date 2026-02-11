package com.chatapp.chat_service.domain.repository;

import com.chatapp.chat_service.domain.model.GroupMessage;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupMessageRepository extends ReactiveCassandraRepository<GroupMessage , UUID> {
}

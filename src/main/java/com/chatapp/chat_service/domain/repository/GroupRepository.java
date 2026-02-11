package com.chatapp.chat_service.domain.repository;

import com.chatapp.chat_service.domain.model.Group;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface GroupRepository extends ReactiveCassandraRepository<Group, UUID> {
    Flux<Group> findAllBy(PageRequest pageRequest);
}
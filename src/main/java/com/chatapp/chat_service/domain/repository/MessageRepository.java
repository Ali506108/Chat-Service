package com.chatapp.chat_service.domain.repository;

import com.chatapp.chat_service.domain.model.Message;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface MessageRepository extends ReactiveCassandraRepository<Message , UUID> {

    Flux<Message> findByChatId(UUID chatId);

    Flux<Message> findByChatId(UUID chatId, Pageable pageable);
}
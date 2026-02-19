package com.chatapp.chat_service.infrastructure.persistence.jpa;

import com.chatapp.chat_service.domain.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MessageService {

    Mono<Message> sendAMessage(Message message);

    Mono<Message> getMessageByID(UUID messageID);

    Flux<Message> getMessageByChatID(UUID chatID);

    Flux<Message> getRecentMessages(UUID chatID , int limit);

}

package com.chatapp.chat_service.domain.service;

import com.chatapp.chat_service.domain.model.Message;
import com.chatapp.chat_service.domain.repository.MessageRepository;
import com.chatapp.chat_service.infrastructure.persistence.jpa.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;


    @Override
    public Mono<Message> sendAMessage(Message message) {

        return repository.save(message)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable ->
                        Mono.error(new RuntimeException("Error saving message"))
                );
    }

    @Override
    public Mono<Message> getMessageByID(UUID messageID) {
        return repository.findById(messageID)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable ->
                        Mono.error(new RuntimeException("Error fetching message"))
                );
    }

    @Override
    public Flux<Message> getMessageByChatID(UUID chatID) {
        return repository.findByChatId(chatID)
                .doOnNext(msg ->
                        log.debug("Retrieved message: {}" , msg.getMessageID())
                )
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable ->
                        Flux.error(new RuntimeException("Error fetching messages"))
                );
    }

    @Override
    public Flux<Message> getRecentMessages(UUID chatID, int limit) {
        if(limit <1 || limit > 1000) {
            return Flux.error(new IllegalArgumentException("Limit 1 .. 1000"));
        }
        Pageable pageable = PageRequest.of(0 ,
                limit , Sort.by("messageId").descending());

        return repository.findByChatId(chatID , pageable)
                .timeout(Duration.ofSeconds(5))
                .doOnNext(msg ->
                        log.debug("Retrieved message: {}", msg.getMessageID())
                )
                .onErrorResume(throwable ->
                        Flux.error(new RuntimeException("Error fetching recent messages"))
                );
    }
}

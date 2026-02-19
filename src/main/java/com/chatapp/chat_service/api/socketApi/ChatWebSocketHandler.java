package com.chatapp.chat_service.api.socketApi;

import com.chatapp.chat_service.domain.model.Message;
import com.chatapp.chat_service.infrastructure.persistence.jpa.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MessageService service;
    private final Sinks.Many<String> chatSink = Sinks.many().multicast().directBestEffort();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this::parseAndPrepareMessage)
                .flatMap(msg -> service.sendAMessage(msg).
                        doOnNext(saved -> chatSink.emitNext(toJson(saved), Sinks.EmitFailureHandler.FAIL_FAST))
                        .onErrorResume(error -> {
                            log.error("Ошибка сохранения сообщения: {}", error.getMessage());
                            return Mono.empty();
                        })
                ).then();


        Mono<Void> output = session.send(chatSink.asFlux().map(session::textMessage));


        return Mono.zip(input , output).then();
    }

    private Mono<Message> parseAndPrepareMessage(String json) {
        try {
            Message msg = objectMapper.readValue(json, Message.class);
            if (msg.getMessageID() == null) msg.setMessageID(UUID.randomUUID());
            if (msg.getCreatedAt() == null) msg.setCreatedAt(Instant.now());
            if (msg.getUpdatedAt() == null) msg.setUpdatedAt(Instant.now());
            return Mono.just(msg);
        } catch (Exception e) {
            log.error("Ошибка парсинга JSON: {}", e.getMessage());
            return Mono.empty(); // Игнорируем плохой JSON, не ломая сокет
        }
    }

    private String toJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{}";
        }
    }
}
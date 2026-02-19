package com.chatapp.chat_service.domain.service;

import com.chatapp.chat_service.domain.model.Message;
import com.chatapp.chat_service.domain.repository.MessageRepository;
import com.datastax.oss.driver.internal.core.type.codec.TimeUuidCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @InjectMocks
    MessageServiceImpl messageService;

    @Mock
    MessageRepository repository;

    private UUID chatId;
    private UUID messageId1;
    private UUID messageId2;
    private Message msg1;
    private Message msg2;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        messageId1 = UUID.randomUUID();
        messageId2 = UUID.randomUUID();

        msg1 = Message.builder()
                .messageID(messageId1)
                .chatId(chatId)
                .senderID(UUID.randomUUID())
                .content("Hello")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        msg2 = Message.builder()
                .messageID(messageId2)
                .chatId(chatId)
                .senderID(UUID.randomUUID())
                .content("Hello how are you ?")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Successfully send a message")
    void sendAMessage() {
        when(repository.save(any(Message.class))).thenReturn(Mono.just(msg1));

        StepVerifier.create(messageService.sendAMessage(msg1))
                .assertNext(msg -> {
                    assertThat(msg).isSameAs(msg1);
                    assertThat(msg.getMessageID()).isEqualTo(messageId1);
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("Successfully get message by ID")
    void getMessageByID() {

        when(repository.findById(messageId1)).thenReturn(Mono.just(msg1));

        StepVerifier.create(messageService.getMessageByID(messageId1))
                .assertNext(msg -> {
                    assertThat(msg).isSameAs(msg1);
                    assertThat(msg.getMessageID()).isEqualTo(messageId1);
                }).verifyComplete();

    }

    @Test
    void getMessageByChatID() {
        when(repository.findByChatId(chatId)).thenReturn(Flux.just(msg1, msg2));

        StepVerifier.create(messageService.getMessageByChatID(chatId))
                .assertNext(msg -> assertThat(msg.getChatId()).isEqualTo(chatId))
                .assertNext(msg -> assertThat(msg.getChatId()).isEqualTo(chatId))
                .verifyComplete();

    }

    @Test
    void getRecentMessages() {
        when(repository.findByChatId(chatId, PageRequest.of(0, 10, Sort.by("messageId").descending())))
                .thenReturn(Flux.just(msg1, msg2));

        StepVerifier.create(messageService.getRecentMessages(chatId, 10))
                .expectNextCount(2)
                .verifyComplete();
    }
}
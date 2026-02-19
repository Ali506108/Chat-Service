package com.chatapp.chat_service.infrastructure.persistence.socket;

import com.chatapp.chat_service.api.socketApi.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import java.util.Map;

@Slf4j
@Configuration
public class SocketConfig {

    @Bean
    public HandlerMapping webSocketMapping(ChatWebSocketHandler socketHandler) {
        log.info(">>>> Регистрация WebSocket пути: /ws/chat");
        Map<String, WebSocketHandler> map = Map.of("/ws/chat", socketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        mapping.setCorsConfigurations(Map.of("/**", corsConfig));

        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        log.info(">>>> WebSocket адаптер создан");
        return new WebSocketHandlerAdapter(new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy()));
    }
}

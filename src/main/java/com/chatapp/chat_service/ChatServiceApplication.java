package com.chatapp.chat_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ChatServiceApplication {

	static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ChatServiceApplication.class, args);
	}


	@Bean
	WebFilter traceWebFilter() {
		return (exchange, chain) -> chain.filter(exchange);
	}

}

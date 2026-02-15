package com.chatapp.chat_service.domain.exception;

public class ServiceExceptions extends RuntimeException {

    public ServiceExceptions(String message) {
        super(message);
    }

    public ServiceExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}

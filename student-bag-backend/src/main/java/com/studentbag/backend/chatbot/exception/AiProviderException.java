package com.studentbag.backend.chatbot.exception;

public class AiProviderException extends AiException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
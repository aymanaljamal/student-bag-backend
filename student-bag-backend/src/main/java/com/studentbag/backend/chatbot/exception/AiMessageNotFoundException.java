package com.studentbag.backend.chatbot.exception;

public class AiMessageNotFoundException extends AiException {

    public AiMessageNotFoundException(Long messageId) {
        super("AI message not found with id: " + messageId);
    }
}
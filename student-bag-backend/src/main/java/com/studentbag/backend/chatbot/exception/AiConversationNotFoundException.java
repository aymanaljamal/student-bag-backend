package com.studentbag.backend.chatbot.exception;

public class AiConversationNotFoundException extends AiException {

    public AiConversationNotFoundException(Long conversationId) {
        super("AI conversation not found with id: " + conversationId);
    }
}
package com.studentbag.backend.chatbot.exception;
public class AiContextBuildException extends AiException {

    public AiContextBuildException(String message) {
        super(message);
    }

    public AiContextBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
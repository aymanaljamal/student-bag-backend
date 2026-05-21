package com.studentbag.backend.chatbot.exception;
public class AiStudentNotFoundException extends AiException {
    public AiStudentNotFoundException(String identifier) {
        super("Student not found for AI assistant: " + identifier);
    }
}
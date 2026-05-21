package com.studentbag.backend.chatbot.exception;
public class AiUsageLimitExceededException extends AiException {

    public AiUsageLimitExceededException(Long studentId) {
        super("AI usage limit exceeded for student id: " + studentId);
    }
}
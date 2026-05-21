package com.studentbag.backend.chatbot.service;

public interface AiUsageService {

    void logUsage(
            Long studentId,
            Long conversationId,
            String model,
            Integer inputTokens,
            Integer outputTokens
    );

    boolean canStudentUseAi(Long studentId);
}
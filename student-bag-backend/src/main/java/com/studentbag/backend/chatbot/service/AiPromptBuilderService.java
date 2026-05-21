package com.studentbag.backend.chatbot.service;

import com.studentbag.backend.chatbot.dto.context.StudentAiContextDto;

public interface AiPromptBuilderService {

    String buildSystemPrompt();

    String buildStudentPrompt(
            StudentAiContextDto context,
            String userQuestion
    );
}
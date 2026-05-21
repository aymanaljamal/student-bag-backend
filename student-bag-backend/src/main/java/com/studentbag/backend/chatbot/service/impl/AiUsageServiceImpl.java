package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.entity.AiConversation;
import com.studentbag.backend.chatbot.entity.AiUsageLog;
import com.studentbag.backend.chatbot.exception.AiConversationNotFoundException;
import com.studentbag.backend.chatbot.repository.AiConversationRepository;
import com.studentbag.backend.chatbot.repository.AiUsageLogRepository;
import com.studentbag.backend.chatbot.service.AiUsageService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiUsageServiceImpl implements AiUsageService {

    private final AiUsageLogRepository usageLogRepository;
    private final AiConversationRepository conversationRepository;
    private final StudentRepository studentRepository;

    @Override
    public void logUsage(
            Long studentId,
            Long conversationId,
            String model,
            Integer inputTokens,
            Integer outputTokens
    ) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        AiConversation conversation = null;

        if (conversationId != null) {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new AiConversationNotFoundException(conversationId));
        }

        int safeInputTokens = inputTokens == null ? 0 : inputTokens;
        int safeOutputTokens = outputTokens == null ? 0 : outputTokens;

        AiUsageLog log = AiUsageLog.builder()
                .student(student)
                .conversation(conversation)
                .model(model)
                .inputTokens(safeInputTokens)
                .outputTokens(safeOutputTokens)
                .totalTokens(safeInputTokens + safeOutputTokens)
                .build();

        usageLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canStudentUseAi(Long studentId) {
        return true;
    }
}
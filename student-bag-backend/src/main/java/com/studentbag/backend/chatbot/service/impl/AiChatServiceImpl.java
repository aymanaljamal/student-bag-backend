package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.dto.openai.OpenAiChatRequest;
import com.studentbag.backend.chatbot.dto.openai.OpenAiMessageDto;
import com.studentbag.backend.chatbot.dto.request.StudentAiChatRequest;
import com.studentbag.backend.chatbot.dto.response.StudentAiChatResponse;
import com.studentbag.backend.chatbot.entity.AiConversation;
import com.studentbag.backend.chatbot.entity.AiMessage;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.chatbot.entity.enums.AiMessageRole;
import com.studentbag.backend.chatbot.exception.AiAccessDeniedException;
import com.studentbag.backend.chatbot.exception.AiConversationNotFoundException;
import com.studentbag.backend.chatbot.exception.AiStudentNotFoundException;
import com.studentbag.backend.chatbot.exception.AiUsageLimitExceededException;
import com.studentbag.backend.chatbot.repository.AiConversationRepository;
import com.studentbag.backend.chatbot.repository.AiMessageRepository;
import com.studentbag.backend.chatbot.service.*;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AiChatServiceImpl implements AiChatService {

    private final StudentRepository studentRepository;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    private final AiIntentResolverService intentResolverService;
    private final StudentAiContextService studentAiContextService;
    private final AiPromptBuilderService promptBuilderService;
    private final OpenAiClientService openAiClientService;
    private final AiUsageService aiUsageService;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    @Override
    public StudentAiChatResponse chat(StudentAiChatRequest request) {
        Student student = getCurrentStudent();

        if (!aiUsageService.canStudentUseAi(student.getId())) {
            throw new AiUsageLimitExceededException(student.getId());
        }

        AiConversationType type = request.getType() != null
                ? request.getType()
                : intentResolverService.resolveIntent(request.getMessage());

        AiConversation conversation = getOrCreateConversation(
                student,
                request.getConversationId(),
                type,
                request.getMessage()
        );

        AiMessage userMessage = saveUserMessage(conversation, request.getMessage());

        var context = request.getNoteId() != null
                ? studentAiContextService.buildQuizContextFromNote(student.getId(), request.getNoteId())
                : studentAiContextService.buildContextForQuestion(student.getId(), request.getMessage());

        String systemPrompt = promptBuilderService.buildSystemPrompt();
        String studentPrompt = promptBuilderService.buildStudentPrompt(context, request.getMessage());

        var openAiResponse = openAiClientService.sendChatRequest(
                OpenAiChatRequest.builder()
                        .model(model)
                        .temperature(0.3)
                        .maxTokens(900)
                        .messages(List.of(
                                OpenAiMessageDto.builder()
                                        .role("system")
                                        .content(systemPrompt)
                                        .build(),
                                OpenAiMessageDto.builder()
                                        .role("user")
                                        .content(studentPrompt)
                                        .build()
                        ))
                        .build()
        );

        AiMessage assistantMessage = saveAssistantMessage(
                conversation,
                openAiResponse.getAnswer(),
                openAiResponse.getInputTokens(),
                openAiResponse.getOutputTokens(),
                openAiResponse.getTotalTokens()
        );

        aiUsageService.logUsage(
                student.getId(),
                conversation.getId(),
                openAiResponse.getModel(),
                openAiResponse.getInputTokens(),
                openAiResponse.getOutputTokens()
        );

        return StudentAiChatResponse.builder()
                .conversationId(conversation.getId())
                .userMessageId(userMessage.getId())
                .assistantMessageId(assistantMessage.getId())
                .type(type)
                .answer(openAiResponse.getAnswer())
                .inputTokens(openAiResponse.getInputTokens())
                .outputTokens(openAiResponse.getOutputTokens())
                .totalTokens(openAiResponse.getTotalTokens())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AiConversation getOrCreateConversation(
            Student student,
            Long conversationId,
            AiConversationType type,
            String firstMessage
    ) {
        if (conversationId != null) {
            AiConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new AiConversationNotFoundException(conversationId));

            if (!conversation.getStudent().getId().equals(student.getId())) {
                throw new AiAccessDeniedException("You do not have access to this AI conversation.");
            }

            return conversation;
        }

        String title = buildConversationTitle(firstMessage);

        AiConversation conversation = AiConversation.builder()
                .student(student)
                .title(title)
                .type(type)
                .archived(false)
                .build();

        return conversationRepository.save(conversation);
    }

    private AiMessage saveUserMessage(AiConversation conversation, String content) {
        AiMessage message = AiMessage.builder()
                .conversation(conversation)
                .role(AiMessageRole.USER)
                .content(content)
                .build();

        return messageRepository.save(message);
    }

    private AiMessage saveAssistantMessage(
            AiConversation conversation,
            String content,
            Integer inputTokens,
            Integer outputTokens,
            Integer totalTokens
    ) {
        AiMessage message = AiMessage.builder()
                .conversation(conversation)
                .role(AiMessageRole.ASSISTANT)
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(totalTokens)
                .build();

        return messageRepository.save(message);
    }

    private Student getCurrentStudent() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new AiStudentNotFoundException(email));
    }

    private String buildConversationTitle(String message) {
        if (message == null || message.isBlank()) {
            return "New AI Conversation";
        }

        String normalized = message.replaceAll("\\s+", " ").trim();

        if (normalized.length() <= 60) {
            return normalized;
        }

        return normalized.substring(0, 60) + "...";
    }
}
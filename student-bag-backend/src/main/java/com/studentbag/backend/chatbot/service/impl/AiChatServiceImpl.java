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
import com.studentbag.backend.chatbot.service.AiChatService;
import com.studentbag.backend.chatbot.service.AiIntentResolverService;
import com.studentbag.backend.chatbot.service.AiPromptBuilderService;
import com.studentbag.backend.chatbot.service.AiUsageService;
import com.studentbag.backend.chatbot.service.OpenAiClientService;
import com.studentbag.backend.chatbot.service.StudentAiContextService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AiChatServiceImpl implements AiChatService {
    private static final int HISTORY_MESSAGES_LIMIT = 16;
    private static final int HISTORY_MESSAGE_MAX_CHARS = 6000;

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
        validateRequest(request);

        Student student = getCurrentStudent();

        if (!aiUsageService.canStudentUseAi(student.getId())) {
            throw new AiUsageLimitExceededException(student.getId());
        }

        AiConversationType type = resolveConversationType(request);

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

        List<OpenAiMessageDto> openAiMessages = buildOpenAiMessages(
                conversation.getId(),
                userMessage.getId(),
                systemPrompt,
                studentPrompt
        );

        var openAiResponse = openAiClientService.sendChatRequest(
                OpenAiChatRequest.builder()
                        .model(model)
                        .temperature(0.3)
                        .maxTokens(1200)
                        .messages(openAiMessages)
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

    private void validateRequest(StudentAiChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AI chat request is required.");
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("AI chat message is required.");
        }
    }

    private AiConversationType resolveConversationType(StudentAiChatRequest request) {
        if (request.getType() != null) {
            return request.getType();
        }

        return intentResolverService.resolveIntent(request.getMessage());
    }

    private List<OpenAiMessageDto> buildOpenAiMessages(
            Long conversationId,
            Long currentUserMessageId,
            String systemPrompt,
            String currentStudentPrompt
    ) {
        List<OpenAiMessageDto> messages = new ArrayList<>();

        messages.add(OpenAiMessageDto.builder()
                .role("system")
                .content(systemPrompt + "\n\n" + buildConversationMemoryRules())
                .build());

        List<AiMessage> previousMessages = loadPreviousConversationMessages(
                conversationId,
                currentUserMessageId
        );

        for (AiMessage message : previousMessages) {
            messages.add(OpenAiMessageDto.builder()
                    .role(toOpenAiRole(message.getRole()))
                    .content(limitText(message.getContent(), HISTORY_MESSAGE_MAX_CHARS))
                    .build());
        }


        messages.add(OpenAiMessageDto.builder()
                .role("user")
                .content(currentStudentPrompt)
                .build());

        return messages;
    }

    private List<AiMessage> loadPreviousConversationMessages(
            Long conversationId,
            Long currentUserMessageId
    ) {
        List<AiMessage> allMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        List<AiMessage> cleanMessages = allMessages.stream()
                .filter(message -> message != null)
                .filter(message -> message.getId() != null)
                .filter(message -> !message.getId().equals(currentUserMessageId))
                .filter(message -> message.getContent() != null)
                .filter(message -> !message.getContent().isBlank())
                .toList();

        int fromIndex = Math.max(0, cleanMessages.size() - HISTORY_MESSAGES_LIMIT);

        return cleanMessages.subList(fromIndex, cleanMessages.size());
    }

    private String buildConversationMemoryRules() {
        return """
                
                Conversation memory rules:
                - Use only the messages from the current conversation history.
                - Do not assume access to other conversations.
                - Do not mix quizzes, answers, tasks, notes, or resources from other conversations.
                - If the student refers to a previous answer, quiz, explanation, or result, look for it only in the current conversation history.
                - If the needed previous content is not available in the current conversation history, say that clearly and ask the student to send it again.
                
                Quiz generation rules:
                - When generating a quiz, base it only on the provided student context, notes, resources, schedule, and file previews.
                - Do not invent courses, lectures, files, grades, or schedule items.
                - If the requested course or material is not found in the provided context, say that clearly.
                - Unless the student asks for answers immediately, present the quiz questions first and tell the student to send answers for correction.
                - Make quiz questions numbered clearly.
                - For MCQ questions, label choices as A, B, C, D.
                
                Quiz correction rules:
                - If the student sends answers for a previous quiz, correct using the quiz found in the current conversation history.
                - If multiple quizzes exist in the same conversation, correct the most recent quiz unless the student clearly identifies an older quiz.
                - If the target quiz is unclear, ask the student which quiz they want corrected.
                - Do not use quizzes from other conversations.
                - Do not invent missing quiz questions or correct answers.
                - If a question or answer is missing, mark it as "Needs review" instead of guessing.
                - Return the result in a clean academic format.
                - Include:
                  1. Score
                  2. Total questions
                  3. Percentage
                  4. Table with question number, student answer, correct answer, status
                  5. Short explanation for each wrong answer
                  6. Final study advice
                - If the previous quiz is not available in the current conversation history, ask the student to send the quiz questions again.
                """;
    }

    private String toOpenAiRole(AiMessageRole role) {
        if (role == null) {
            return "user";
        }

        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            default -> "user";
        };
    }

    private String limitText(String text, int maxChars) {
        if (text == null) {
            return "";
        }

        String clean = text.trim();

        if (clean.length() <= maxChars) {
            return clean;
        }

        return clean.substring(0, maxChars)
                + "\n\n[Message shortened to avoid exceeding AI context limit]";
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

            validateConversationOwnership(conversation, student.getId());

            if (conversation.getType() == AiConversationType.GENERAL
                    && type != null
                    && type != AiConversationType.GENERAL) {
                conversation.setType(type);
                conversationRepository.save(conversation);
            }

            return conversation;
        }

        String title = buildConversationTitle(firstMessage);

        AiConversation conversation = AiConversation.builder()
                .student(student)
                .title(title)
                .type(type != null ? type : AiConversationType.GENERAL)
                .archived(false)
                .build();

        return conversationRepository.save(conversation);
    }

    private void validateConversationOwnership(
            AiConversation conversation,
            Long studentId
    ) {
        if (conversation == null) {
            throw new AiAccessDeniedException("You do not have access to this AI conversation.");
        }

        if (conversation.getStudent() == null ||
                conversation.getStudent().getId() == null ||
                !conversation.getStudent().getId().equals(studentId)) {
            throw new AiAccessDeniedException("You do not have access to this AI conversation.");
        }
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
                .inputTokens(safeToken(inputTokens))
                .outputTokens(safeToken(outputTokens))
                .totalTokens(safeToken(totalTokens))
                .build();

        return messageRepository.save(message);
    }

    private Integer safeToken(Integer value) {
        return value == null ? 0 : value;
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
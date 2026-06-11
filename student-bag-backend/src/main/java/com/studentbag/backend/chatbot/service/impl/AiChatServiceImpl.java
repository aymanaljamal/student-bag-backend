package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.dto.context.StudentAiContextDto;
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

    private static final int DEFAULT_AI_MAX_TOKENS = 1200;
    private static final double DEFAULT_AI_TEMPERATURE = 0.3;

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

        syncConversationTypeIfNeeded(conversation, type);

        AiMessage userMessage = saveUserMessage(conversation, request.getMessage());

        StudentAiContextDto context = buildContextForResolvedType(
                student.getId(),
                request,
                type
        );

        String systemPrompt = promptBuilderService.buildSystemPrompt();
        String studentPrompt = promptBuilderService.buildStudentPrompt(
                context,
                request.getMessage()
        );

        List<OpenAiMessageDto> openAiMessages = buildOpenAiMessages(
                conversation.getId(),
                userMessage.getId(),
                systemPrompt,
                studentPrompt,
                type
        );

        var openAiResponse = openAiClientService.sendChatRequest(
                OpenAiChatRequest.builder()
                        .model(model)
                        .temperature(DEFAULT_AI_TEMPERATURE)
                        .maxTokens(DEFAULT_AI_MAX_TOKENS)
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

    /*
     * Important:
     * The backend must not blindly trust the type sent from Flutter.
     *
     * Example:
     * Flutter may send EVENT_HELP while the message says:
     * "يوم الاثنين شو عندي محاضرات؟"
     *
     * In that case, text intent must win and the final type should be SCHEDULE_HELP.
     */
    private AiConversationType resolveConversationType(StudentAiChatRequest request) {
        AiConversationType textType = intentResolverService.resolveIntent(request.getMessage());

        if (textType != null && textType != AiConversationType.GENERAL) {
            return textType;
        }

        if (request.getType() != null) {
            return request.getType();
        }

        return AiConversationType.GENERAL;
    }

    private StudentAiContextDto buildContextForResolvedType(
            Long studentId,
            StudentAiChatRequest request,
            AiConversationType type
    ) {
        if (request.getNoteId() != null) {
            return studentAiContextService.buildQuizContextFromNote(
                    studentId,
                    request.getNoteId()
            );
        }

        if (type == null) {
            return studentAiContextService.buildContextForQuestion(
                    studentId,
                    request.getMessage()
            );
        }

        return switch (type) {
            case SCHEDULE_HELP -> studentAiContextService.buildScheduleContext(studentId);

            case EVENT_HELP -> studentAiContextService.buildEventsContext(studentId);

            case GRADE_ANALYSIS -> studentAiContextService.buildGradesContext(studentId);

            case RESOURCE_SEARCH -> {
                if (shouldUseQuestionAwareContext(request.getMessage())) {
                    yield studentAiContextService.buildContextForQuestion(
                            studentId,
                            request.getMessage()
                    );
                }

                yield studentAiContextService.buildResourcesContext(studentId);
            }

            case NOTE_EXPLANATION -> studentAiContextService.buildContextForQuestion(
                    studentId,
                    request.getMessage()
            );

            case TASK_HELP -> {
                if (shouldUseQuestionAwareContext(request.getMessage())) {
                    yield studentAiContextService.buildContextForQuestion(
                            studentId,
                            request.getMessage()
                    );
                }

                yield studentAiContextService.buildTaskContext(studentId);
            }

            case STUDY_PLAN -> studentAiContextService.buildTodayStudyContext(studentId);

            case QUIZ_GENERATION -> studentAiContextService.buildContextForQuestion(
                    studentId,
                    request.getMessage()
            );

            case DASHBOARD_HELP -> studentAiContextService.buildGeneralContext(studentId);

            case GENERAL -> studentAiContextService.buildContextForQuestion(
                    studentId,
                    request.getMessage()
            );
        };
    }

    private boolean shouldUseQuestionAwareContext(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = normalize(message);

        return containsAny(
                normalized,
                "file",
                "files",
                "pdf",
                "document",
                "attachment",
                "attachments",
                "resource",
                "resources",
                "read",
                "explain",
                "summary",
                "summarize",
                "quiz",
                "questions",
                "test",
                "exam",
                "ملف",
                "ملفات",
                "بي دي اف",
                "مرفق",
                "مرفقات",
                "مصدر",
                "مصادر",
                "اقرا",
                "اقرأ",
                "اشرح",
                "شرح",
                "لخص",
                "تلخيص",
                "ملخص",
                "كويز",
                "اسئلة",
                "اسئله",
                "امتحان",
                "اختبار"
        );
    }

    private List<OpenAiMessageDto> buildOpenAiMessages(
            Long conversationId,
            Long currentUserMessageId,
            String systemPrompt,
            String currentStudentPrompt,
            AiConversationType resolvedType
    ) {
        List<OpenAiMessageDto> messages = new ArrayList<>();

        messages.add(OpenAiMessageDto.builder()
                .role("system")
                .content(
                        systemPrompt
                                + "\n\n"
                                + buildResolvedTypeRules(resolvedType)
                                + "\n\n"
                                + buildConversationMemoryRules()
                )
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

    private String buildResolvedTypeRules(AiConversationType resolvedType) {
        if (resolvedType == null) {
            return "";
        }

        return switch (resolvedType) {
            case SCHEDULE_HELP -> """
                    Current request type: SCHEDULE_HELP.

                    Schedule answer rules:
                    - The student is asking about academic schedule, lectures, classes, rooms, halls, instructors, or days.
                    - Use schedule context only for lectures/classes.
                    - Do not answer using events unless the user clearly asks about events.
                    - If the user asks about a specific day, answer for that day from the schedule context.
                    - If dates are old but weekday names match, explain using the weekday when possible.
                    - Mention course, instructor, room/location, day, and time when available.
                    - If no schedule entries are provided, say that clearly without guessing.
                    """;

            case EVENT_HELP -> """
                    Current request type: EVENT_HELP.

                    Event answer rules:
                    - The student is asking about events, opportunities, registration, workshops, internships, volunteering, or training.
                    - Use events and opportunities context.
                    - Clearly separate registered events from available events.
                    - Mention whether the student is registered when that data is available.
                    - Mention registration availability when available.
                    - Do not mix academic lectures with events.
                    """;

            case GRADE_ANALYSIS -> """
                    Current request type: GRADE_ANALYSIS.

                    Grade answer rules:
                    - The student is asking about GPA, marks, grade calculations, averages, percentages, or performance.
                    - If multiple grade calculations are available and the student did not specify one, ask them to choose by calculation title.
                    - If the student specifies a calculation title, analyze that calculation only.
                    - Mention GPA, percentage, credits, weak courses, strong courses, repeated courses, and practical improvement advice when available.
                    """;

            case RESOURCE_SEARCH -> """
                    Current request type: RESOURCE_SEARCH.

                    Resource answer rules:
                    - The student is asking about files, resources, folders, PDFs, materials, attachments, or links.
                    - Use resources context and file content previews when available.
                    - If the student asks to summarize/explain/read a file, use file contents first.
                    - If only metadata is available, say that only file metadata is available.
                    """;

            case NOTE_EXPLANATION -> """
                    Current request type: NOTE_EXPLANATION.

                    Notes answer rules:
                    - The student is asking about notes, explanation, summaries, simplification, or understanding.
                    - Use notes and attached file previews when available.
                    - Do not invent note content that is not provided.
                    """;

            case TASK_HELP -> """
                    Current request type: TASK_HELP.

                    Task answer rules:
                    - The student is asking about tasks, assignments, deadlines, due dates, todos, or required work.
                    - Prioritize overdue and due-today tasks.
                    - Mention priority, due time, course, subtasks, and attachments when available.
                    """;

            case STUDY_PLAN -> """
                    Current request type: STUDY_PLAN.

                    Study plan rules:
                    - Build a practical study plan using today's schedule, upcoming schedule, tasks, notes, resources, and grades when available.
                    - Prioritize urgent deadlines and weak courses.
                    - Keep the plan realistic and time-aware.
                    """;

            case QUIZ_GENERATION -> """
                    Current request type: QUIZ_GENERATION.

                    Quiz rules:
                    - Generate quiz questions only from provided context, notes, resources, schedule, or file previews.
                    - Do not invent course content.
                    - If the material is missing, ask the student to provide or select the material.
                    """;

            case DASHBOARD_HELP -> """
                    Current request type: DASHBOARD_HELP.

                    Dashboard rules:
                    - Summarize the student's academic status using dashboard, tasks, notes, schedule, events, grades, and resources.
                    - Highlight urgent items first.
                    """;

            case GENERAL -> """
                    Current request type: GENERAL.

                    General rules:
                    - Answer using the provided student context.
                    - If the question clearly belongs to a module, rely on that module's context.
                    - If data is missing, say so clearly.
                    """;
        };
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
                - Do not mix quizzes, answers, tasks, notes, resources, files, grades, schedules, or events from other conversations.
                - If the student refers to a previous answer, quiz, explanation, or result, look for it only in the current conversation history.
                - If the needed previous content is not available in the current conversation history, say that clearly and ask the student to send it again.

                Hallucination prevention rules:
                - Never invent courses, lectures, instructors, rooms, grades, files, tasks, notes, events, opportunities, or registration status.
                - If a requested item is not present in the provided context, say that it is not available in the current context.
                - If the question contains a specific name, match it against the provided context before answering.
                - If multiple possible matches exist, ask the student to choose.

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

    private void syncConversationTypeIfNeeded(
            AiConversation conversation,
            AiConversationType resolvedType
    ) {
        if (conversation == null || resolvedType == null) {
            return;
        }

        if (resolvedType == AiConversationType.GENERAL) {
            return;
        }

        if (conversation.getType() == null ||
                conversation.getType() == AiConversationType.GENERAL ||
                conversation.getType() != resolvedType) {
            conversation.setType(resolvedType);
            conversationRepository.save(conversation);
        }
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

    private AiMessage saveUserMessage(
            AiConversation conversation,
            String content
    ) {
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

    private boolean containsAny(String text, String... keywords) {
        String normalizedText = normalize(text);

        for (String keyword : keywords) {
            String normalizedKeyword = normalize(keyword);

            if (normalizedKeyword.isBlank()) {
                continue;
            }

            if (normalizedText.contains(normalizedKeyword)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ى', 'ي')
                .replace('ة', 'ه')
                .replace('ؤ', 'و')
                .replace('ئ', 'ي')
                .replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
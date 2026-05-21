package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.dto.request.CreateAiConversationRequest;
import com.studentbag.backend.chatbot.dto.response.AiConversationResponse;
import com.studentbag.backend.chatbot.dto.response.AiMessageResponse;
import com.studentbag.backend.chatbot.entity.AiConversation;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.chatbot.exception.AiAccessDeniedException;
import com.studentbag.backend.chatbot.exception.AiConversationNotFoundException;
import com.studentbag.backend.chatbot.exception.AiStudentNotFoundException;
import com.studentbag.backend.chatbot.mapper.AiEntityMapper;
import com.studentbag.backend.chatbot.repository.AiConversationRepository;
import com.studentbag.backend.chatbot.repository.AiMessageRepository;
import com.studentbag.backend.chatbot.service.AiConversationService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final AiEntityMapper aiEntityMapper;

    @Override
    public AiConversationResponse createConversation(CreateAiConversationRequest request) {
        Student student = getCurrentStudent();

        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = "New AI Conversation";
        }

        AiConversation conversation = AiConversation.builder()
                .student(student)
                .title(title)
                .type(request.getType() != null ? request.getType() : AiConversationType.GENERAL)
                .archived(false)
                .build();

        AiConversation saved = conversationRepository.save(conversation);

        return aiEntityMapper.toConversationResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiConversationResponse> getMyConversations() {
        Student student = getCurrentStudent();

        return conversationRepository
                .findByStudentIdAndArchivedFalseOrderByUpdatedAtDesc(student.getId())
                .stream()
                .map(conversation -> {
                    String preview = messageRepository
                            .findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                            .map(message -> aiEntityMapper.preview(message.getContent()))
                            .orElse(null);

                    return aiEntityMapper.toConversationResponse(conversation, preview);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversationResponse getConversation(Long conversationId) {
        Student student = getCurrentStudent();

        AiConversation conversation = findOwnedConversation(conversationId, student.getId());

        String preview = messageRepository
                .findTopByConversationIdOrderByCreatedAtDesc(conversationId)
                .map(message -> aiEntityMapper.preview(message.getContent()))
                .orElse(null);

        return aiEntityMapper.toConversationResponse(conversation, preview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMessageResponse> getConversationMessages(Long conversationId) {
        Student student = getCurrentStudent();

        findOwnedConversation(conversationId, student.getId());

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(aiEntityMapper::toMessageResponse)
                .toList();
    }

    @Override
    public void archiveConversation(Long conversationId) {
        Student student = getCurrentStudent();

        AiConversation conversation = findOwnedConversation(conversationId, student.getId());
        conversation.setArchived(true);

        conversationRepository.save(conversation);
    }

    @Override
    public void deleteConversation(Long conversationId) {
        Student student = getCurrentStudent();

        AiConversation conversation = findOwnedConversation(conversationId, student.getId());

        conversationRepository.delete(conversation);
    }

    private AiConversation findOwnedConversation(Long conversationId, Long studentId) {
        AiConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AiConversationNotFoundException(conversationId));

        if (conversation.getStudent() == null ||
                !conversation.getStudent().getId().equals(studentId)) {
            throw new AiAccessDeniedException("You do not have access to this AI conversation.");
        }

        return conversation;
    }

    private Student getCurrentStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new AiStudentNotFoundException(email));
    }
}
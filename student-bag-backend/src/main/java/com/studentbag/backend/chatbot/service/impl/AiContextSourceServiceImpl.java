package com.studentbag.backend.chatbot.service.impl;
import com.studentbag.backend.chatbot.entity.AiContextSource;
import com.studentbag.backend.chatbot.entity.AiMessage;
import com.studentbag.backend.chatbot.entity.enums.AiContextSourceType;
import com.studentbag.backend.chatbot.exception.AiMessageNotFoundException;
import com.studentbag.backend.chatbot.repository.AiContextSourceRepository;
import com.studentbag.backend.chatbot.repository.AiMessageRepository;
import com.studentbag.backend.chatbot.service.AiContextSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional
public class AiContextSourceServiceImpl implements AiContextSourceService {

    private final AiContextSourceRepository contextSourceRepository;
    private final AiMessageRepository messageRepository;

    @Override
    public void saveSource(
            Long messageId,
            AiContextSourceType sourceType,
            Long sourceId,
            String sourceTitle
    ) {
        if (messageId == null || sourceType == null || sourceId == null) {
            return;
        }

        AiMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AiMessageNotFoundException(messageId));

        AiContextSource source = AiContextSource.builder()
                .message(message)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .sourceTitle(sourceTitle)
                .build();

        contextSourceRepository.save(source);
    }
}
package com.studentbag.backend.chatbot.mapper;

import com.studentbag.backend.chatbot.dto.response.AiConversationResponse;
import com.studentbag.backend.chatbot.dto.response.AiMessageResponse;
import com.studentbag.backend.chatbot.dto.response.AiUsedSourceResponse;
import com.studentbag.backend.chatbot.entity.AiContextSource;
import com.studentbag.backend.chatbot.entity.AiConversation;
import com.studentbag.backend.chatbot.entity.AiMessage;
import org.springframework.stereotype.Component;

@Component
public class AiEntityMapper {

    public AiConversationResponse toConversationResponse(
            AiConversation conversation,
            String lastMessagePreview
    ) {
        if (conversation == null) return null;

        return AiConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .type(conversation.getType())
                .archived(conversation.getArchived())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessagePreview(lastMessagePreview)
                .build();
    }

    public AiMessageResponse toMessageResponse(AiMessage message) {
        if (message == null) return null;

        return AiMessageResponse.builder()
                .id(message.getId())
                .conversationId(
                        message.getConversation() != null
                                ? message.getConversation().getId()
                                : null
                )
                .role(message.getRole())
                .content(message.getContent())
                .inputTokens(message.getInputTokens())
                .outputTokens(message.getOutputTokens())
                .totalTokens(message.getTotalTokens())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public AiUsedSourceResponse toUsedSourceResponse(AiContextSource source) {
        if (source == null) return null;

        return AiUsedSourceResponse.builder()
                .sourceType(source.getSourceType())
                .sourceId(source.getSourceId())
                .sourceTitle(source.getSourceTitle())
                .build();
    }

    public String preview(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String normalized = content.replaceAll("\\s+", " ").trim();

        if (normalized.length() <= 120) {
            return normalized;
        }

        return normalized.substring(0, 120) + "...";
    }
}
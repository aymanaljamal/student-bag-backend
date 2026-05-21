package com.studentbag.backend.chatbot.dto.response;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAiChatResponse {

    private Long conversationId;
    private Long userMessageId;
    private Long assistantMessageId;

    private AiConversationType type;

    private String answer;

    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    private List<AiUsedSourceResponse> usedSources;

    private LocalDateTime createdAt;
}
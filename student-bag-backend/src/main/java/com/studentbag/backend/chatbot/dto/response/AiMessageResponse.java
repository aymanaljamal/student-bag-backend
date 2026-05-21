package com.studentbag.backend.chatbot.dto.response;
import com.studentbag.backend.chatbot.entity.enums.AiMessageRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMessageResponse {

    private Long id;
    private Long conversationId;

    private AiMessageRole role;
    private String content;

    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    private LocalDateTime createdAt;
}
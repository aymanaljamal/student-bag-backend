package com.studentbag.backend.chatbot.dto.response;

import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversationSummaryResponse {

    private Long id;
    private String title;
    private AiConversationType type;
    private LocalDateTime updatedAt;
    private String preview;
}
package com.studentbag.backend.chatbot.dto.response;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsageResponse {

    private Long id;

    private Long studentId;
    private Long conversationId;

    private String model;

    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    private LocalDateTime createdAt;
}
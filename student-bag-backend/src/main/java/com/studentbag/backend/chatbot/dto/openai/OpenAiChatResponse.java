package com.studentbag.backend.chatbot.dto.openai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiChatResponse {

    private String answer;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    private String model;
}
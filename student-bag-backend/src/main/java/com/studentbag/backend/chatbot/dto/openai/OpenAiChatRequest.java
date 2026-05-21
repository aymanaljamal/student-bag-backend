package com.studentbag.backend.chatbot.dto.openai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiChatRequest {

    private String model;

    private List<OpenAiMessageDto> messages;

    private Double temperature;

    private Integer maxTokens;
}
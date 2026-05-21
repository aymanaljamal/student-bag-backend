package com.studentbag.backend.chatbot.dto.openai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiMessageDto {

    private String role;
    private String content;
}
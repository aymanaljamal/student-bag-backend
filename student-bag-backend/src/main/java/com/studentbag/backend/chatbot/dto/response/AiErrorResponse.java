package com.studentbag.backend.chatbot.dto.response;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
}
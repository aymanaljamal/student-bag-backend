package com.studentbag.backend.chatbot.dto.response;

import com.studentbag.backend.chatbot.entity.enums.AiContextSourceType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsedSourceResponse {

    private AiContextSourceType sourceType;
    private Long sourceId;
    private String sourceTitle;
}
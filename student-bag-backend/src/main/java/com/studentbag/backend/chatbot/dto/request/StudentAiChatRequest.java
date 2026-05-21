package com.studentbag.backend.chatbot.dto.request;

import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAiChatRequest {

    private Long conversationId;

    @NotBlank
    @Size(max = 4000)
    private String message;

    private AiConversationType type;

    private Long noteId;
    private Long taskId;
    private Long resourceId;
    private Long eventId;
    private Long scheduleEntryId;
    private Long gradeCalculationId;
}
package com.studentbag.backend.chatbot.dto.request;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAiConversationRequest {

    @Size(max = 150)
    private String title;

    private AiConversationType type;
}
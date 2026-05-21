package com.studentbag.backend.chatbot.entity;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_saved_prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSavedPrompt extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AiConversationType type;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
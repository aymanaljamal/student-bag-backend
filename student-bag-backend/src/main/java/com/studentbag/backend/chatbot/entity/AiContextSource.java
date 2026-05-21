package com.studentbag.backend.chatbot.entity;
import com.studentbag.backend.chatbot.entity.enums.AiContextSourceType;
import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_context_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiContextSource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private AiMessage message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AiContextSourceType sourceType;

    @Column(nullable = false)
    private Long sourceId;

    @Column(length = 255)
    private String sourceTitle;
}
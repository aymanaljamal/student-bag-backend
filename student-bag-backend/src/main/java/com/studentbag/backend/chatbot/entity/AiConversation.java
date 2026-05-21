package com.studentbag.backend.chatbot.entity;

import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private AiConversationType type = AiConversationType.GENERAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
}
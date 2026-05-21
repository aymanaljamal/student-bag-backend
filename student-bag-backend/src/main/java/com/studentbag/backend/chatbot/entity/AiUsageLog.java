package com.studentbag.backend.chatbot.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsageLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private AiConversation conversation;

    @Column(length = 100)
    private String model;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;
}
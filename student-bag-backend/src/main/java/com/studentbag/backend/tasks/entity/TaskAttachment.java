package com.studentbag.backend.tasks.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.tasks.TaskAttachmentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAttachment extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskAttachmentType kind;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 255)
    private String fileName;

    @Column(length = 150)
    private String mimeType;

    private Long fileSizeBytes;

    @Column(length = 20)
    private String extension;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVoiceNote = false;

    private Integer durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
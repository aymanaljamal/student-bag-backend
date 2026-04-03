package com.studentbag.backend.notes.dto.response;

import com.studentbag.backend.domain.enums.NotePriority;
import com.studentbag.backend.domain.enums.NoteType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class NoteResponse {

    private Long id;
    private Long studentId;
    private Long courseId;
    private String title;
    private String contentHtml;

    private Boolean isImportant;
    private Boolean isPinned;
    private Boolean isArchived;
    private Boolean isDeleted;

    private NotePriority priority;
    private NoteType noteType;

    private String color;

    private String tags;
    private List<NoteAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
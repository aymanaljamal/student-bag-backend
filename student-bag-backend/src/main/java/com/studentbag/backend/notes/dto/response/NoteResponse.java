package com.studentbag.backend.notes.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
    private Boolean isDeleted;
    private String tags;
    private List<NoteAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
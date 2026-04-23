package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LinkedNoteSummaryResponse {
    private Long id;
    private String title;
    private String contentHtml;
    private Boolean isImportant;
    private Boolean isPinned;
    private String color;
    private String tags;
    private String priority;
    private String noteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LinkedNoteAttachmentResponse> attachments;
}
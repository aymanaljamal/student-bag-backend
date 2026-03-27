package com.studentbag.backend.notes.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoteAttachmentResponse {

    private Long id;
    private Long noteId;
    private String type;
    private String url;
    private String fileName;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
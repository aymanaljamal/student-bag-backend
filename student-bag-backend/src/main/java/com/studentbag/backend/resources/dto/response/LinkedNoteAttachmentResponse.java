package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkedNoteAttachmentResponse {
    private Long id;
    private String type;
    private String url;
    private String fileName;
    private Integer durationSeconds;
    private Long fileSizeBytes;
}
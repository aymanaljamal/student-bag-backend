package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkedTaskAttachmentResponse {
    private Long id;
    private String url;
    private String fileName;
    private String mimeType;
    private String extension;
    private Long fileSizeBytes;
}
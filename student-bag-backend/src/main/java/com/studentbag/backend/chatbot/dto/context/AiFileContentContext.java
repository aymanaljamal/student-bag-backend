package com.studentbag.backend.chatbot.dto.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiFileContentContext {

    private String ownerType;
    private Long ownerId;

    private String title;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;

    private String contentPreview;
}
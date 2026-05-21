package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentAiContext {

    private Long id;

    private String type;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;

    private Boolean isVoiceNote;
    private Integer durationSeconds;
}
package com.studentbag.backend.tasks.dto.response;
import com.studentbag.backend.domain.enums.tasks.TaskAttachmentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAttachmentResponse {

    private Long id;
    private TaskAttachmentType kind;
    private String url;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;
    private String extension;
    private Boolean isVoiceNote;
    private Integer durationSeconds;
    private LocalDateTime createdAt;
}
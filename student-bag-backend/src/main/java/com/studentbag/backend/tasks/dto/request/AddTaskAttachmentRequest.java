package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.tasks.TaskAttachmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTaskAttachmentRequest {

    @NotNull
    private TaskAttachmentType kind;

    @NotBlank
    @Size(max = 2000)
    private String url;

    @Size(max = 255)
    private String fileName;

    @Size(max = 150)
    private String mimeType;

    private Long fileSizeBytes;

    @Size(max = 20)
    private String extension;

    @Builder.Default
    private Boolean isVoiceNote = false;

    private Integer durationSeconds;
}
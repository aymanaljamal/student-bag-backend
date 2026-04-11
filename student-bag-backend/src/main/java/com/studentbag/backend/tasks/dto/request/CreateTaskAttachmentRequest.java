package com.studentbag.backend.tasks.dto.request;
import com.studentbag.backend.domain.enums.TaskAttachmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskAttachmentRequest {

    @NotNull
    private TaskAttachmentType kind;

    @NotBlank
    private String url;

    private String fileName;

    private String mimeType;

    private Long fileSizeBytes;

    private String extension;

    @Builder.Default
    private Boolean isVoiceNote = false;

    private Integer durationSeconds;
}
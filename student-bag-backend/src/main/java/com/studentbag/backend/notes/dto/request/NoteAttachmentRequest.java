package com.studentbag.backend.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteAttachmentRequest {

    @NotNull
    private Long noteId;

    @NotBlank
    private String type;

    @NotBlank
    private String url;

    @NotBlank
    private String fileName;

    private Integer durationSeconds;

    private Long fileSizeBytes;
}
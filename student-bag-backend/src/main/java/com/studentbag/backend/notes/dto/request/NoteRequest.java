package com.studentbag.backend.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {

    @NotNull
    private Long studentId;

    private Long courseId;

    @NotBlank
    private String title;

    @NotBlank
    private String contentHtml;

    private Boolean isImportant = false;

    private Boolean isDeleted = false;

    private String tags;
}
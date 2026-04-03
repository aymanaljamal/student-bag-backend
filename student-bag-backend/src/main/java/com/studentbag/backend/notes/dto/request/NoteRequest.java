package com.studentbag.backend.notes.dto.request;

import com.studentbag.backend.domain.enums.NotePriority;
import com.studentbag.backend.domain.enums.NoteType;
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

    private String contentJson;

    private Boolean isImportant = false;

    private Boolean isPinned = false;

    private Boolean isArchived = false;

    private Boolean isDeleted = false;

    private NotePriority priority = NotePriority.MEDIUM;

    private NoteType noteType = NoteType.GENERAL;

    private String color;

    private String tags;
}
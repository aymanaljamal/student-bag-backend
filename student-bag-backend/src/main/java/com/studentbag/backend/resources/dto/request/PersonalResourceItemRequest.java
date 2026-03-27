package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.ContentFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalResourceItemRequest {

    @NotNull
    private Long ownerStudentId;

    private Long folderId;

    @NotBlank
    private String title;

    @NotNull
    private ContentFormat format;

    private Boolean isExamPreparation = false;

    private Boolean isImportant = false;

    private Long linkedAdminResourceId;

    private Long noteId;

    private String fileUrl;
}
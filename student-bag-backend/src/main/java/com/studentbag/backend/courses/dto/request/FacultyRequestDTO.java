package com.studentbag.backend.courses.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacultyRequestDTO {

    @NotBlank
    private String nameArabic;

    private String nameEnglish;

    @NotNull
    private Long institutionId;

    private Boolean isActive = true;
}
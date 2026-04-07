package com.studentbag.backend.courses.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentRequestDTO {

    @NotBlank
    private String nameArabic;

    private String nameEnglish;
    private String programNameArabic;
    private String programNameEnglish;

    @NotNull
    private Long facultyId;

    private Boolean isActive = true;
}
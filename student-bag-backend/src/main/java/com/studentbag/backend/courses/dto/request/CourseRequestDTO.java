package com.studentbag.backend.courses.dto.request;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating/updating Course
 */
@Data
public class CourseRequestDTO {

    private String externalId;

    @NotNull
    private String code;

    @NotNull
    private String nameArabic;

    private String nameEnglish;

    private String description;

    @NotNull
    private Integer creditHours;

    @NotNull
    private AcademicLevel level;

    private String programNameArabic;
    private String programNameEnglish;

    @NotNull
    private Long institutionId;

    private Long departmentId;

    private Boolean isActive = true;
}
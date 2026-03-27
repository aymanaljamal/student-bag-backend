package com.studentbag.backend.courses.dto.request;

import com.studentbag.backend.domain.enums.AcademicLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Integer creditHours;

    @NotNull
    private AcademicLevel level;

    @NotNull
    private Long institutionId;

    private Boolean isActive = true;
}
package com.studentbag.backend.courses.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseSectionRequest {

    @NotNull
    private Long courseId;

    @NotNull
    private Long termId;

    @NotBlank
    private String sectionNumber;

    private Long instructorId;

    @NotNull
    private Integer capacity;

    private Integer enrolled = 0;

    private Boolean isOfficial = true;
}
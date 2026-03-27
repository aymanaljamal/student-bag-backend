package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.domain.enums.AcademicLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer creditHours;
    private AcademicLevel level;
    private Long institutionId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
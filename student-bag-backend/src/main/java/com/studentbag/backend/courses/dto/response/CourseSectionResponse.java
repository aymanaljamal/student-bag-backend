package com.studentbag.backend.courses.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseSectionResponse {

    private Long id;
    private Long courseId;
    private Long termId;
    private String sectionNumber;
    private Long instructorId;
    private Integer capacity;
    private Integer enrolled;
    private Boolean isOfficial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.studentbag.backend.courses.dto.request;

import com.studentbag.backend.domain.enums.SectionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating/updating CourseSection
 */
@Data
public class CourseSectionRequestDTO {

    private String externalId;

    @NotNull
    private Long courseId;

    @NotNull
    private Long termId;

    @NotNull
    private String sectionNumber;

    @NotNull
    private SectionType sectionType;

    private Long instructorId;

    private Long parentLectureSectionId;

    private Integer capacity = 0;

    private Integer enrolled = 0;

    private Boolean isOfficial = true;
}
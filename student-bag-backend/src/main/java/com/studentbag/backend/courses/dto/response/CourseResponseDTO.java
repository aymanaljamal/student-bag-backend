package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for returning Course data (with optional sections)
 */
@Data
@Builder
public class CourseResponseDTO {

    private Long id;

    private String externalId;
    private String code;

    private String nameArabic;
    private String nameEnglish;

    private String description;

    private Integer creditHours;
    private AcademicLevel level;

    private String programNameArabic;
    private String programNameEnglish;

    private Long institutionId;
    private Long departmentId;

    private Boolean isActive;

    /**
     * Optional: list of simplified sections
     */
    private List<CourseSectionSimpleDTO> sections;

}
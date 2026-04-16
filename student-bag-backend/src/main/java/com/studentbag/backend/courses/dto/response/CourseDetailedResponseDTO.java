package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.courses.dto.CourseSectionDetailedDTO;
import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseDetailedResponseDTO {
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
    private List<CourseSectionDetailedDTO> sections;
}
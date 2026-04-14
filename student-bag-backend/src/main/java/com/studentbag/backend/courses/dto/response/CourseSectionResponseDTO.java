package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.domain.enums.courses.SectionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseSectionResponseDTO {

    private Long id;

    private String externalId;

    private Long courseId;
    private Long termId;

    private String sectionNumber;
    private SectionType sectionType;

    private Long instructorId;
    private String instructorNameArabic;
    private String instructorNameEnglish;

    private Long parentLectureSectionId;

    private Integer capacity;
    private Integer enrolled;

    private Integer availableSeats;

    private Boolean isOfficial;
}
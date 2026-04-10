package com.studentbag.backend.courses.dto;

import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseSectionDetailedDTO {
    private Long id;
    private String externalId;
    private Long courseId;
    private Long termId;
    private String sectionNumber;
    private String sectionType;

    private Long instructorId;
    private String instructorName;
    private String instructorNameArabic;
    private String instructorNameEnglish;

    private Long parentLectureSectionId;
    private Integer capacity;
    private Integer enrolled;
    private Integer availableSeats;
    private Boolean isOfficial;

    private List<ClassSessionResponseDTO> classSessions;
}
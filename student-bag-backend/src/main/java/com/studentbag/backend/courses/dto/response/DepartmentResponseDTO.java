package com.studentbag.backend.courses.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponseDTO {

    private Long id;
    private String externalId;
    private String nameArabic;
    private String nameEnglish;
    private String programNameArabic;
    private String programNameEnglish;
    private Long facultyId;
    private Boolean isActive;
}
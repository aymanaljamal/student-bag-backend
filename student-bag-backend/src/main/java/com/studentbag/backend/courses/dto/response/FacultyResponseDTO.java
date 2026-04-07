package com.studentbag.backend.courses.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyResponseDTO {

    private Long id;
    private String externalId;
    private String nameArabic;
    private String nameEnglish;
    private Long institutionId;
    private Boolean isActive;
}
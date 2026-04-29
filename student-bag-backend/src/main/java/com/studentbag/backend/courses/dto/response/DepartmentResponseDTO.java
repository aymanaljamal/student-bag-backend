package com.studentbag.backend.courses.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDTO {

    private Long id;
    private String externalId;

    private String nameArabic;
    private String nameEnglish;

    private String programNameArabic;
    private String programNameEnglish;

    private Long facultyId;
    private String facultyNameArabic;
    private String facultyNameEnglish;

    private Long institutionId;
    private String institutionName;

    private Boolean isActive;
}
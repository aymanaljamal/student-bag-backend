package com.studentbag.backend.courses.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyResponseDTO {

    private Long id;
    private String externalId;

    private String nameArabic;
    private String nameEnglish;

    private Long institutionId;
    private String institutionName;

    private Boolean isActive;
}
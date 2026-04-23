package com.studentbag.backend.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveScheduleCourseDTO {
    private Long id;
    private String externalId;
    private String code;
    private String nameArabic;
    private String nameEnglish;
    private String description;
    private Integer creditHours;
}
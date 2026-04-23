package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceCourseSummaryResponse {
    private Long id;
    private String externalId;
    private String code;
    private String nameArabic;
    private String nameEnglish;
    private String description;
    private Integer creditHours;
}
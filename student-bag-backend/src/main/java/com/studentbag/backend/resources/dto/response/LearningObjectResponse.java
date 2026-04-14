package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import com.studentbag.backend.domain.enums.ContentFormat;
import com.studentbag.backend.domain.enums.LanguageCode;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LearningObjectResponse {

    private Long id;
    private String title;
    private String description;
    private String keywords;
    private LanguageCode language;
    private ContentFormat format;
    private String difficulty;
    private String intendedEndUserRole;
    private AcademicLevel educationalLevel;
    private ResourceType resourceType;
    private Integer typicalLearningTimeMinutes;
    private String url;
    private String thumbnailUrl;
    private Boolean isActive;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
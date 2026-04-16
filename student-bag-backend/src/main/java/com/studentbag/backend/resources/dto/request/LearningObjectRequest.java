package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import com.studentbag.backend.domain.enums.ContentFormat;
import com.studentbag.backend.domain.enums.LanguageCode;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LearningObjectRequest {

    @NotBlank
    private String title;

    private String description;

    private String keywords;

    private LanguageCode language;

    @NotNull
    private ContentFormat format;

    private String difficulty;

    private String intendedEndUserRole;

    private AcademicLevel educationalLevel;

    private ResourceType resourceType;

    private Integer typicalLearningTimeMinutes;

    private String url;

    private String thumbnailUrl;

    private Boolean isActive = true;

    private Long createdByUserId;
}
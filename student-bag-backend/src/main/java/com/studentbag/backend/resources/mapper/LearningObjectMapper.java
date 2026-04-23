package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.resources.dto.response.ResourceCourseSummaryResponse;
import com.studentbag.backend.resources.entity.LearningObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LearningObjectMapper {

    public ResourceCourseSummaryResponse toCourseSummary(LearningObject entity) {
        if (entity == null || entity.getCourse() == null) {
            return null;
        }

        return ResourceCourseSummaryResponse.builder()
                .id(entity.getCourse().getId())
                .externalId(entity.getCourse().getExternalId())
                .code(entity.getCourse().getCode())
                .nameArabic(entity.getCourse().getNameArabic())
                .nameEnglish(entity.getCourse().getNameEnglish())
                .description(entity.getCourse().getDescription())
                .creditHours(entity.getCourse().getCreditHours())
                .build();
    }
}
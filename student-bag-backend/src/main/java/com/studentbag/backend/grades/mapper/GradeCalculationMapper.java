package com.studentbag.backend.grades.mapper;

import com.studentbag.backend.grades.dto.response.GradeCalculationResponse;
import com.studentbag.backend.grades.dto.response.GradeCourseItemResponse;
import com.studentbag.backend.grades.entity.GradeCalculation;
import com.studentbag.backend.grades.entity.GradeCourseItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeCalculationMapper {

    public GradeCalculationResponse toResponse(GradeCalculation entity) {
        List<GradeCourseItemResponse> items = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return GradeCalculationResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .sourceScheduleId(entity.getSourceSchedule() != null ? entity.getSourceSchedule().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .sourceType(entity.getSourceType())
                .inputType(entity.getInputType())
                .calculationType(entity.getCalculationType())
                .repeatPolicy(entity.getRepeatPolicy())
                .percentageToGpaPolicy(entity.getPercentageToGpaPolicy())
                .gpaScaleMax(entity.getGpaScaleMax())
                .markScaleMax(entity.getMarkScaleMax())
                .autoGenerateSubjectNames(entity.getAutoGenerateSubjectNames())
                .includePassFailCourses(entity.getIncludePassFailCourses())
                .includeWithdrawnCourses(entity.getIncludeWithdrawnCourses())
                .isLocked(entity.getIsLocked())
                .calculatedGpa(entity.getCalculatedGpa())
                .calculatedPercentage(entity.getCalculatedPercentage())
                .totalQualityPoints(entity.getTotalQualityPoints())
                .totalCredits(entity.getTotalCredits())
                .subjectCount(entity.getSubjectCount())
                .items(items)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public GradeCourseItemResponse toItemResponse(GradeCourseItem item) {
        return GradeCourseItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourse() != null ? item.getCourse().getId() : null)
                .courseSectionId(item.getCourseSection() != null ? item.getCourseSection().getId() : null)
                .courseCode(item.getCourseCodeSnapshot())
                .courseName(item.getCourseNameSnapshot())
                .creditHours(item.getCreditHoursSnapshot())
                .enteredValue(item.getEnteredValue())
                .enteredOutOf(item.getEnteredOutOf())
                .letterGrade(item.getLetterGrade())
                .normalizedPercentage(item.getNormalizedPercentage())
                .gradePoints(item.getGradePoints())
                .qualityPoints(item.getQualityPoints())
                .orderIndex(item.getOrderIndex())
                .courseStatus(item.getCourseStatus())
                .includedInCalculation(item.getIncludedInCalculation())
                .isManualEntry(item.getIsManualEntry())
                .isFromSchedule(item.getIsFromSchedule())
                .isRepeatedCourse(item.getIsRepeatedCourse())
                .notes(item.getNotes())
                .build();
    }
}
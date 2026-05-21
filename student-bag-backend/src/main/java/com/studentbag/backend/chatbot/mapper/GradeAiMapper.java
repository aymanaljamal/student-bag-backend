package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.GradeAiContext;
import com.studentbag.backend.chatbot.dto.context.GradeCourseAiContext;
import com.studentbag.backend.grades.entity.GradeCalculation;
import com.studentbag.backend.grades.entity.GradeCourseItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeAiMapper {

    public GradeAiContext toContext(GradeCalculation calculation) {
        if (calculation == null) return null;

        return GradeAiContext.builder()
                .calculationId(calculation.getId())
                .title(calculation.getTitle())
                .calculationType(calculation.getCalculationType() != null ? calculation.getCalculationType().name() : null)
                .inputType(calculation.getInputType() != null ? calculation.getInputType().name() : null)
                .repeatPolicy(calculation.getRepeatPolicy() != null ? calculation.getRepeatPolicy().name() : null)

                .calculatedGpa(calculation.getCalculatedGpa())
                .calculatedPercentage(calculation.getCalculatedPercentage())
                .totalCredits(calculation.getTotalCredits())
                .subjectCount(calculation.getSubjectCount())

                .courses(calculation.getItems() == null ? List.of() :
                        calculation.getItems().stream()
                                .map(this::mapCourse)
                                .toList())
                .build();
    }

    private GradeCourseAiContext mapCourse(GradeCourseItem item) {
        return GradeCourseAiContext.builder()
                .id(item.getId())
                .courseCode(item.getCourseCodeSnapshot())
                .courseName(item.getCourseNameSnapshot())
                .creditHours(item.getCreditHoursSnapshot())
                .enteredValue(item.getEnteredValue())
                .normalizedPercentage(item.getNormalizedPercentage())
                .gradePoints(item.getGradePoints())
                .letterGrade(item.getLetterGrade())
                .status(item.getCourseStatus() != null ? item.getCourseStatus().name() : null)
                .repeatedCourse(item.getIsRepeatedCourse())
                .includedInCalculation(item.getIncludedInCalculation())
                .build();
    }
}
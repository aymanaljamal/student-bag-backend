package com.studentbag.backend.grades.dto.request;

import com.studentbag.backend.domain.enums.grades.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGradeCalculationRequest {

    private String title;
    private String description;

    @Builder.Default
    private GradeCalculationSource sourceType = GradeCalculationSource.MANUAL;

    @Builder.Default
    private GradeInputType inputType = GradeInputType.MARK_OUT_OF_100;

    @Builder.Default
    private GradeCalculationType calculationType = GradeCalculationType.SEMESTER_GPA;

    @Builder.Default
    private GradeRepeatPolicy repeatPolicy = GradeRepeatPolicy.LAST_ATTEMPT;

    @Builder.Default
    private PercentageToGpaPolicy percentageToGpaPolicy = PercentageToGpaPolicy.PALESTINIAN_DEFAULT;

    @Builder.Default
    private BigDecimal gpaScaleMax = new BigDecimal("4.00");

    @Builder.Default
    private BigDecimal markScaleMax = new BigDecimal("100.00");

    @Builder.Default
    private Boolean autoGenerateSubjectNames = true;

    @Builder.Default
    private Boolean includePassFailCourses = false;

    @Builder.Default
    private Boolean includeWithdrawnCourses = false;

    @Builder.Default
    private List<GradeCourseItemRequest> items = new ArrayList<>();
}
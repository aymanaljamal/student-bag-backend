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
public class UpdateGradeCalculationRequest {

    private String title;
    private String description;

    private GradeInputType inputType;
    private GradeCalculationType calculationType;
    private GradeRepeatPolicy repeatPolicy;
    private PercentageToGpaPolicy percentageToGpaPolicy;

    private BigDecimal gpaScaleMax;
    private BigDecimal markScaleMax;

    private Boolean autoGenerateSubjectNames;
    private Boolean includePassFailCourses;
    private Boolean includeWithdrawnCourses;
    private Boolean isLocked;

    @Builder.Default
    private List<GradeCourseItemRequest> items = new ArrayList<>();
}
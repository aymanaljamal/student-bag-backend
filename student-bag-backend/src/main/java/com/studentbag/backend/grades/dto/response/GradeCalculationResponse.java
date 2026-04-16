package com.studentbag.backend.grades.dto.response;

import com.studentbag.backend.domain.enums.grades.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCalculationResponse {

    private Long id;
    private Long studentId;
    private Long sourceScheduleId;

    private String title;
    private String description;

    private GradeCalculationSource sourceType;
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

    private BigDecimal calculatedGpa;
    private BigDecimal calculatedPercentage;
    private BigDecimal totalQualityPoints;
    private BigDecimal totalCredits;
    private Integer subjectCount;

    @Builder.Default
    private List<GradeCourseItemResponse> items = new ArrayList<>();

    private GradeInsightsResponse insights;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
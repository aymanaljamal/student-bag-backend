package com.studentbag.backend.grades.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeWhatIfResponse {

    private boolean possible;
    private String scope;
    private String targetType;
    private BigDecimal targetValue;

    private BigDecimal currentValue;
    private BigDecimal requiredSemesterValue;
    private BigDecimal requiredAverageOnRemaining;
    private BigDecimal maxPossibleValue;

    private BigDecimal completedCredits;
    private BigDecimal gradedCredits;
    private BigDecimal remainingCredits;
    private BigDecimal totalCredits;

    private String message;

    @Builder.Default
    private List<WhatIfRequiredCourseDTO> requiredPerCourse = new ArrayList<>();

    @Builder.Default
    private List<String> notes = new ArrayList<>();
}
package com.studentbag.backend.grades.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseImpactDTO {
    private Long itemId;
    private String courseName;
    private BigDecimal creditHours;
    private BigDecimal currentPercentage;
    private BigDecimal currentGpa;
    private BigDecimal impactScore;
}
package com.studentbag.backend.grades.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeInsightsResponse {

    private String summary;
    private String overallLevel;

    @Builder.Default
    private List<GradeAdviceDTO> advice = new ArrayList<>();

    @Builder.Default
    private List<CourseImpactDTO> weakestCourses = new ArrayList<>();

    @Builder.Default
    private List<CourseImpactDTO> highestImpactCourses = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
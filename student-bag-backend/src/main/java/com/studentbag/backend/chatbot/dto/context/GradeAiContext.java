package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeAiContext {

    private Long calculationId;

    private String title;
    private String calculationType;
    private String inputType;
    private String repeatPolicy;

    private BigDecimal calculatedGpa;
    private BigDecimal calculatedPercentage;
    private BigDecimal totalCredits;

    private Integer subjectCount;

    private List<GradeCourseAiContext> courses;
}
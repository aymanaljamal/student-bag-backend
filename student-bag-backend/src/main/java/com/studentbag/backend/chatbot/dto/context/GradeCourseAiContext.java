package com.studentbag.backend.chatbot.dto.context;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeCourseAiContext {

    private Long id;

    private String courseCode;
    private String courseName;

    private BigDecimal creditHours;

    private BigDecimal enteredValue;
    private BigDecimal normalizedPercentage;
    private BigDecimal gradePoints;

    private String letterGrade;
    private String status;

    private Boolean repeatedCourse;
    private Boolean includedInCalculation;
}
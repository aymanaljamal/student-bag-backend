package com.studentbag.backend.grades.dto.request;

import com.studentbag.backend.domain.enums.grades.GradeCourseStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCourseItemRequest {

    private Long courseId;
    private Long courseSectionId;

    private String courseCode;
    private String courseName;
    private BigDecimal creditHours;

    private BigDecimal enteredValue;
    private BigDecimal enteredOutOf;
    private String letterGrade;

    private Integer orderIndex;
    private GradeCourseStatus courseStatus;

    private Boolean includedInCalculation;
    private Boolean isRepeatedCourse;
    private String notes;
}
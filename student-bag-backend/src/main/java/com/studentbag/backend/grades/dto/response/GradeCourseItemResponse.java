package com.studentbag.backend.grades.dto.response;

import com.studentbag.backend.domain.enums.grades.GradeCourseStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCourseItemResponse {

    private Long id;
    private Long courseId;
    private Long courseSectionId;

    private String courseCode;
    private String courseName;
    private BigDecimal creditHours;

    private BigDecimal enteredValue;
    private BigDecimal enteredOutOf;
    private String letterGrade;

    private BigDecimal normalizedPercentage;
    private BigDecimal gradePoints;
    private BigDecimal qualityPoints;

    private Integer orderIndex;
    private GradeCourseStatus courseStatus;

    private Boolean includedInCalculation;
    private Boolean isManualEntry;
    private Boolean isFromSchedule;
    private Boolean isRepeatedCourse;
    private String notes;
}
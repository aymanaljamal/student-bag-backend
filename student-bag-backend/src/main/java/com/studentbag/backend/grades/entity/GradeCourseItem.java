package com.studentbag.backend.grades.entity;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.domain.enums.grades.GradeCourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "grade_course_items",
        indexes = {
                @Index(name = "idx_grade_item_calc", columnList = "calculation_id"),
                @Index(name = "idx_grade_item_course", columnList = "course_id"),
                @Index(name = "idx_grade_item_section", columnList = "course_section_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calculation_id", nullable = false)
    private GradeCalculation calculation;

    // روابط اختيارية لو المادة من الجدول أو من الكورسات
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id")
    private CourseSection courseSection;

    // Snapshot ثابت وقت الحفظ
    @Column(name = "course_code_snapshot", length = 100)
    private String courseCodeSnapshot;

    @Column(name = "course_name_snapshot", nullable = false, length = 200)
    private String courseNameSnapshot;

    @Column(name = "credit_hours_snapshot", precision = 6, scale = 2, nullable = false)
    private BigDecimal creditHoursSnapshot;

    @Column(name = "order_index")
    private Integer orderIndex;

    // القيمة التي أدخلها الطالب
    @Column(name = "entered_value", precision = 10, scale = 4)
    private BigDecimal enteredValue;

    // مثال: 100 أو 4
    @Column(name = "entered_out_of", precision = 10, scale = 4)
    private BigDecimal enteredOutOf;

    // قيم موحدة نحسبها ونخزنها
    @Column(name = "normalized_percentage", precision = 10, scale = 4)
    private BigDecimal normalizedPercentage;

    @Column(name = "grade_points", precision = 10, scale = 4)
    private BigDecimal gradePoints;

    @Column(name = "quality_points", precision = 12, scale = 4)
    private BigDecimal qualityPoints;

    @Column(name = "letter_grade", length = 10)
    private String letterGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status", nullable = false, length = 30)
    @Builder.Default
    private GradeCourseStatus courseStatus = GradeCourseStatus.COMPLETED;

    @Column(name = "included_in_calculation", nullable = false)
    @Builder.Default
    private Boolean includedInCalculation = true;

    @Column(name = "is_manual_entry", nullable = false)
    @Builder.Default
    private Boolean isManualEntry = true;

    @Column(name = "is_from_schedule", nullable = false)
    @Builder.Default
    private Boolean isFromSchedule = false;

    @Column(name = "is_repeated_course", nullable = false)
    @Builder.Default
    private Boolean isRepeatedCourse = false;

    @Column(length = 500)
    private String notes;
}
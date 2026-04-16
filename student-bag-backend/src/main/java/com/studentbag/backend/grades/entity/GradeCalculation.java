package com.studentbag.backend.grades.entity;

import com.studentbag.backend.domain.enums.grades.*;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "grade_calculations",
        indexes = {
                @Index(name = "idx_grade_calc_student", columnList = "student_id"),
                @Index(name = "idx_grade_calc_student_updated", columnList = "student_id, updated_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_schedule_id")
    private StudentSchedule sourceSchedule;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    @Builder.Default
    private GradeCalculationSource sourceType = GradeCalculationSource.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 30)
    @Builder.Default
    private GradeInputType inputType = GradeInputType.MARK_OUT_OF_100;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 30)
    @Builder.Default
    private GradeCalculationType calculationType = GradeCalculationType.SEMESTER_GPA;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_policy", nullable = false, length = 30)
    @Builder.Default
    private GradeRepeatPolicy repeatPolicy = GradeRepeatPolicy.LAST_ATTEMPT;

    @Enumerated(EnumType.STRING)
    @Column(name = "percentage_to_gpa_policy", nullable = false, length = 30)
    @Builder.Default
    private PercentageToGpaPolicy percentageToGpaPolicy = PercentageToGpaPolicy.PALESTINIAN_DEFAULT;

    @Column(name = "gpa_scale_max", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal gpaScaleMax = new BigDecimal("4.00");

    @Column(name = "mark_scale_max", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal markScaleMax = new BigDecimal("100.00");

    @Column(name = "auto_generate_subject_names", nullable = false)
    @Builder.Default
    private Boolean autoGenerateSubjectNames = true;

    @Column(name = "include_pass_fail_courses", nullable = false)
    @Builder.Default
    private Boolean includePassFailCourses = false;

    @Column(name = "include_withdrawn_courses", nullable = false)
    @Builder.Default
    private Boolean includeWithdrawnCourses = false;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "calculated_gpa", precision = 10, scale = 4)
    private BigDecimal calculatedGpa;

    @Column(name = "calculated_percentage", precision = 10, scale = 4)
    private BigDecimal calculatedPercentage;

    @Column(name = "total_quality_points", precision = 12, scale = 4)
    private BigDecimal totalQualityPoints;

    @Column(name = "total_credits", precision = 10, scale = 2)
    private BigDecimal totalCredits;

    @Column(name = "subject_count")
    private Integer subjectCount;

    @OneToMany(
            mappedBy = "calculation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @OrderBy("orderIndex ASC, id ASC")
    private List<GradeCourseItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addItem(GradeCourseItem item) {
        if (item == null) return;
        item.setCalculation(this);
        this.items.add(item);
    }

    public void removeItem(GradeCourseItem item) {
        if (item == null) return;
        item.setCalculation(null);
        this.items.remove(item);
    }
}
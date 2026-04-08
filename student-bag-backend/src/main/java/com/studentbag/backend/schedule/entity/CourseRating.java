package com.studentbag.backend.schedule.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "course_ratings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_id", "course_id"},
                name = "uq_rating_student_course"
        ))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "difficulty_rating", nullable = false)
    @Min(1) @Max(5)
    private Integer difficultyRating;

    public boolean isHighDifficulty() {
        return difficultyRating != null && difficultyRating >= 4;
    }
}
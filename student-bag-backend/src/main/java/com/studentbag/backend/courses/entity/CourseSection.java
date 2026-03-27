package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.instructor.entity.Instructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "course_sections")
@Getter
@Setter
public class CourseSection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(nullable = false, length = 20)
    private String sectionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer enrolled = 0;

    @Column(nullable = false)
    private Boolean isOfficial = true;

    public boolean hasCapacity() {
        return enrolled < capacity;
    }

    public void enroll() {
        if (!hasCapacity()) {
            throw new IllegalStateException("Section is full.");
        }
        enrolled++;
    }

    public void drop() {
        if (enrolled <= 0) {
            throw new IllegalStateException("No enrolled students to drop.");
        }
        enrolled--;
    }

    public int getEnrolledCount() {
        return enrolled;
    }
}
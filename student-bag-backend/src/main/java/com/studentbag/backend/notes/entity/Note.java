package com.studentbag.backend.notes.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notes")
@Getter
@Setter
public class Note extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentHtml;

    @Column(nullable = false)
    private Boolean isImportant = false;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(length = 500)
    private String tags;

    public void markImportant() {
        this.isImportant = true;
    }

    public void archive() {
        this.isDeleted = true;
    }
}
package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personal_resource_folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalResourceFolder extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private PersonalResourceFolder parentFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Builder.Default
    @Column(name = "is_root", nullable = false)
    private Boolean isRoot = false;

    @Builder.Default
    @Column(name = "is_system_generated", nullable = false)
    private Boolean isSystemGenerated = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Builder.Default
    @Column(name = "show_linked_notes", nullable = false)
    private Boolean showLinkedNotes = true;

    @Builder.Default
    @Column(name = "show_linked_tasks", nullable = false)
    private Boolean showLinkedTasks = true;
}
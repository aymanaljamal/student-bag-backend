package com.studentbag.backend.tasks.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.tasks.TaskPriority;
import com.studentbag.backend.domain.enums.tasks.TaskRecurrenceType;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime dueDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;
    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.ACTIVE;
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    private LocalDateTime completedAt;

    private Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskRecurrenceType recurrenceType = TaskRecurrenceType.NONE;

    @Column(nullable = false)
    @Builder.Default
    private Integer recurrenceInterval = 1;

    private LocalDateTime recurrenceLastGeneratedAt;

    private LocalDateTime nextOccurrenceAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany
    @JoinTable(
            name = "task_label_links",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @Builder.Default
    private Set<TaskLabel> labels = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Subtask> subtasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("remindAt ASC")
    @Builder.Default
    private List<TaskReminder> reminders = new ArrayList<>();
}
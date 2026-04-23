package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_objects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningObject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceCategory category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
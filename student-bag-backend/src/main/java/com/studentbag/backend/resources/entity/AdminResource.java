package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.VisibilityScope;
import com.studentbag.backend.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_resources")
@Getter
@Setter
public class AdminResource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_object_id", nullable = false)
    private LearningObject learningObject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String gradeOrLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityScope visibilityScope;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private Boolean isApproved = false;

    @Column(name = "approved_by_admin_id")
    private Long approvedByAdminId;

    private LocalDateTime approvedAt;

    public void approve(Long adminId) {
        this.isApproved = true;
        this.approvedByAdminId = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void updateVersion() {
        this.version = this.version + 1;
    }
}
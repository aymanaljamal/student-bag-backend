package com.studentbag.backend.parentlink.entity;
import com.studentbag.backend.domain.enums.ParentLinkStatus;
import com.studentbag.backend.parent.entity.Parent;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parent_student_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParentLinkStatus status;

    private boolean canViewGradesDetailed = true;
    private boolean canViewNotesSummary = true;
    private boolean receivePeriodicReports = true;

    private String reportFrequency = "WEEKLY";

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime revokedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ParentLinkStatus.PENDING;
        }
    }
}
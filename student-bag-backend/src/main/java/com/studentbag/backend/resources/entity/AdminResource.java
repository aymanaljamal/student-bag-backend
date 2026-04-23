package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResource extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ResourceApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "uploaded_by_type", nullable = false, length = 20)
    private ResourceOwnerType uploadedByType;

    @Column(name = "uploaded_by_id", nullable = false)
    private Long uploadedById;

    @Column(name = "approved_by_admin_id")
    private Long approvedByAdminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_object_id")
    private LearningObject learningObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Builder.Default
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
}
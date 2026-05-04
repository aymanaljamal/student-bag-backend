package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personal_resource_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalResourceItem extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private PersonalResourceFolder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "external_link", columnDefinition = "TEXT")
    private String externalLink;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "mime_type", columnDefinition = "TEXT")
    private String mimeType;

    @Column(name = "file_name", columnDefinition = "TEXT")
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "copied_from_admin_resource_id")
    private Long copiedFromAdminResourceId;

    @Column(name = "copied_from_personal_item_id")
    private Long copiedFromPersonalItemId;

    @Column(name = "linked_note_id")
    private Long linkedNoteId;

    @Column(name = "linked_task_id")
    private Long linkedTaskId;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;
}
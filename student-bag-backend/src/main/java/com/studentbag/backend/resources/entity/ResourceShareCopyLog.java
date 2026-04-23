package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_share_copy_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceShareCopyLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "source_admin_resource_id")
    private Long sourceAdminResourceId;

    @Column(name = "source_personal_item_id")
    private Long sourcePersonalItemId;

    @Column(name = "target_folder_id")
    private Long targetFolderId;

    @Column(name = "created_personal_item_id")
    private Long createdPersonalItemId;
}
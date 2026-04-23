package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;

import com.studentbag.backend.domain.enums.resources.ResourceActionType;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_approval_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceApprovalAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_resource_id", nullable = false)
    private AdminResource adminResource;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ResourceActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ResourceOwnerType actorType;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(columnDefinition = "TEXT")
    private String note;
}
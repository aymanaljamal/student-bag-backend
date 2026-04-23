package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.resources.dto.response.ResourceApprovalActionResponse;
import com.studentbag.backend.resources.entity.ResourceApprovalAction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceApprovalActionMapper {

    public ResourceApprovalActionResponse toResponse(ResourceApprovalAction entity) {
        if (entity == null) {
            return null;
        }

        return ResourceApprovalActionResponse.builder()
                .id(entity.getId())
                .adminResourceId(entity.getAdminResource() != null ? entity.getAdminResource().getId() : null)
                .actionType(entity.getActionType())
                .actorType(entity.getActorType())
                .actorId(entity.getActorId())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.resources.ResourceActionType;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResourceApprovalActionResponse {
    private Long id;
    private Long adminResourceId;
    private ResourceActionType actionType;
    private ResourceOwnerType actorType;
    private Long actorId;
    private String note;
    private LocalDateTime createdAt;
}
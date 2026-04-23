package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceOperationResponse {
    private Long targetId;
    private String operation;
    private Boolean success;
    private String message;
}
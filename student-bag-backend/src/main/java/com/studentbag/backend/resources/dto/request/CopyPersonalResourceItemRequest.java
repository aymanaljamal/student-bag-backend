package com.studentbag.backend.resources.dto.request;

import lombok.Data;

@Data
public class CopyPersonalResourceItemRequest {
    private Long targetFolderId;
}
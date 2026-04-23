package com.studentbag.backend.resources.dto.request;

import lombok.Data;

@Data
public class CopyAdminResourceToPersonalRequest {
    private Long targetFolderId;
}
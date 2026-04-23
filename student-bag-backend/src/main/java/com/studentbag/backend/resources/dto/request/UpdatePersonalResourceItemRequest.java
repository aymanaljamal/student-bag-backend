package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import lombok.Data;

@Data
public class UpdatePersonalResourceItemRequest {

    private String title;
    private String description;
    private ResourceCategory category;
    private Long folderId;
    private Long courseId;

    private String fileUrl;
    private String externalLink;
    private String thumbnailUrl;
    private String mimeType;
    private String fileName;
    private Long fileSizeBytes;

    private Boolean isArchived;
}
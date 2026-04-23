package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import lombok.Data;

@Data
public class UpdateAdminResourceRequest {

    private String title;
    private String description;
    private ResourceType resourceType;
    private ResourceCategory category;
    private Long courseId;
    private Long learningObjectId;

    private String fileUrl;
    private String externalLink;
    private String thumbnailUrl;
    private String mimeType;
    private String fileName;
    private Long fileSizeBytes;
}
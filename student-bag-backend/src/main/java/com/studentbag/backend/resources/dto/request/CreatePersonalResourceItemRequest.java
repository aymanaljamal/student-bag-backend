package com.studentbag.backend.resources.dto.request;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePersonalResourceItemRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private ResourceType resourceType;

    @NotNull
    private ResourceCategory category;

    private Long folderId;
    private Long courseId;

    private String fileUrl;
    private String externalLink;
    private String thumbnailUrl;
    private String mimeType;
    private String fileName;
    private Long fileSizeBytes;
}
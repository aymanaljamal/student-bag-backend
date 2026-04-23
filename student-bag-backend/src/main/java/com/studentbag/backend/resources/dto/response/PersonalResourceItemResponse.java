package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonalResourceItemResponse {
    private Long id;
    private String title;
    private String description;

    private ResourceType resourceType;
    private ResourceCategory category;

    private Long studentId;
    private Long folderId;

    private Long courseId;
    private String courseCode;
    private String courseNameArabic;
    private String courseNameEnglish;

    private String fileUrl;
    private String externalLink;
    private String thumbnailUrl;
    private String mimeType;
    private String fileName;
    private Long fileSizeBytes;

    private Long copiedFromAdminResourceId;
    private Long copiedFromPersonalItemId;

    private Long linkedNoteId;
    private Long linkedTaskId;

    private Boolean isDeleted;
    private Boolean isArchived;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
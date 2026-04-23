package com.studentbag.backend.resources.dto.response;

import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminResourceResponse {
    private Long id;
    private String title;
    private String description;
    private ResourceType resourceType;
    private ResourceCategory category;
    private ResourceApprovalStatus approvalStatus;

    private ResourceOwnerType uploadedByType;
    private Long uploadedById;
    private Long approvedByAdminId;

    private Long courseId;
    private String courseCode;
    private String courseNameArabic;
    private String courseNameEnglish;

    private Long learningObjectId;
    private String learningObjectTitle;

    private String fileUrl;
    private String externalLink;
    private String thumbnailUrl;
    private String mimeType;
    private String fileName;
    private Long fileSizeBytes;

    private Boolean isVisible;
    private Boolean isDeleted;
    private String adminNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
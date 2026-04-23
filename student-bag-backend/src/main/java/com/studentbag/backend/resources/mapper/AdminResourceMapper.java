package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.resources.dto.request.CreateAdminResourceRequest;
import com.studentbag.backend.resources.dto.request.UpdateAdminResourceRequest;
import com.studentbag.backend.resources.dto.response.AdminResourceResponse;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.LearningObject;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AdminResourceMapper {

    public AdminResource toEntity(CreateAdminResourceRequest request) {
        if (request == null) {
            return null;
        }

        return AdminResource.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .resourceType(request.getResourceType())
                .category(request.getCategory())
                .fileUrl(request.getFileUrl())
                .externalLink(request.getExternalLink())
                .thumbnailUrl(request.getThumbnailUrl())
                .mimeType(request.getMimeType())
                .fileName(request.getFileName())
                .fileSizeBytes(request.getFileSizeBytes())
                .approvalStatus(ResourceApprovalStatus.PENDING)
                .isVisible(false)
                .isDeleted(false)
                .build();
    }

    public void updateEntity(AdminResource entity, UpdateAdminResourceRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getResourceType() != null) {
            entity.setResourceType(request.getResourceType());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getFileUrl() != null) {
            entity.setFileUrl(request.getFileUrl());
        }
        if (request.getExternalLink() != null) {
            entity.setExternalLink(request.getExternalLink());
        }
        if (request.getThumbnailUrl() != null) {
            entity.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getMimeType() != null) {
            entity.setMimeType(request.getMimeType());
        }
        if (request.getFileName() != null) {
            entity.setFileName(request.getFileName());
        }
        if (request.getFileSizeBytes() != null) {
            entity.setFileSizeBytes(request.getFileSizeBytes());
        }
    }

    public AdminResourceResponse toResponse(AdminResource entity) {
        if (entity == null) {
            return null;
        }

        LearningObject learningObject = entity.getLearningObject();

        return AdminResourceResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .resourceType(entity.getResourceType())
                .category(entity.getCategory())
                .approvalStatus(entity.getApprovalStatus())
                .uploadedByType(entity.getUploadedByType())
                .uploadedById(entity.getUploadedById())
                .approvedByAdminId(entity.getApprovedByAdminId())

                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseCode(entity.getCourse() != null ? entity.getCourse().getCode() : null)
                .courseNameArabic(entity.getCourse() != null ? entity.getCourse().getNameArabic() : null)
                .courseNameEnglish(entity.getCourse() != null ? entity.getCourse().getNameEnglish() : null)

                .learningObjectId(learningObject != null ? learningObject.getId() : null)
                .learningObjectTitle(learningObject != null ? learningObject.getTitle() : null)

                .fileUrl(entity.getFileUrl())
                .externalLink(entity.getExternalLink())
                .thumbnailUrl(entity.getThumbnailUrl())
                .mimeType(entity.getMimeType())
                .fileName(entity.getFileName())
                .fileSizeBytes(entity.getFileSizeBytes())

                .isVisible(entity.getIsVisible())
                .isDeleted(entity.getIsDeleted())
                .adminNotes(entity.getAdminNotes())

                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
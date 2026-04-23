package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.resources.dto.request.CreatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.response.PersonalResourceItemResponse;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PersonalResourceItemMapper {

    public PersonalResourceItem toEntity(CreatePersonalResourceItemRequest request) {
        if (request == null) {
            return null;
        }

        return PersonalResourceItem.builder()
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
                .isDeleted(false)
                .isArchived(false)
                .build();
    }

    public void updateEntity(PersonalResourceItem entity, UpdatePersonalResourceItemRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
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
        if (request.getIsArchived() != null) {
            entity.setIsArchived(request.getIsArchived());
        }
    }

    public PersonalResourceItemResponse toResponse(PersonalResourceItem entity) {
        if (entity == null) {
            return null;
        }

        return PersonalResourceItemResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())

                .resourceType(entity.getResourceType())
                .category(entity.getCategory())

                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .folderId(entity.getFolder() != null ? entity.getFolder().getId() : null)

                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseCode(entity.getCourse() != null ? entity.getCourse().getCode() : null)
                .courseNameArabic(entity.getCourse() != null ? entity.getCourse().getNameArabic() : null)
                .courseNameEnglish(entity.getCourse() != null ? entity.getCourse().getNameEnglish() : null)

                .fileUrl(entity.getFileUrl())
                .externalLink(entity.getExternalLink())
                .thumbnailUrl(entity.getThumbnailUrl())
                .mimeType(entity.getMimeType())
                .fileName(entity.getFileName())
                .fileSizeBytes(entity.getFileSizeBytes())

                .copiedFromAdminResourceId(entity.getCopiedFromAdminResourceId())
                .copiedFromPersonalItemId(entity.getCopiedFromPersonalItemId())

                .linkedNoteId(entity.getLinkedNoteId())
                .linkedTaskId(entity.getLinkedTaskId())

                .isDeleted(entity.getIsDeleted())
                .isArchived(entity.getIsArchived())

                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
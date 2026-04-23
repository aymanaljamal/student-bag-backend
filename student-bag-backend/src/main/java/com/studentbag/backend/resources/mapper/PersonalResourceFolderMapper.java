package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.resources.dto.request.CreatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderResponse;
import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PersonalResourceFolderMapper {

    public PersonalResourceFolder toEntity(CreatePersonalResourceFolderRequest request) {
        if (request == null) {
            return null;
        }

        return PersonalResourceFolder.builder()
                .name(request.getName())
                .description(request.getDescription())
                .showLinkedNotes(request.getShowLinkedNotes() == null || request.getShowLinkedNotes())
                .showLinkedTasks(request.getShowLinkedTasks() == null || request.getShowLinkedTasks())
                .isArchived(false)
                .isDeleted(false)
                .isRoot(false)
                .isSystemGenerated(false)
                .build();
    }

    public void updateEntity(PersonalResourceFolder entity, UpdatePersonalResourceFolderRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getShowLinkedNotes() != null) {
            entity.setShowLinkedNotes(request.getShowLinkedNotes());
        }
        if (request.getShowLinkedTasks() != null) {
            entity.setShowLinkedTasks(request.getShowLinkedTasks());
        }
        if (request.getIsArchived() != null) {
            entity.setIsArchived(request.getIsArchived());
        }
    }

    public PersonalResourceFolderResponse toResponse(
            PersonalResourceFolder entity,
            int childFolderCount,
            int itemCount
    ) {
        if (entity == null) {
            return null;
        }

        return PersonalResourceFolderResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())

                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .parentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null)

                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseCode(entity.getCourse() != null ? entity.getCourse().getCode() : null)
                .courseNameArabic(entity.getCourse() != null ? entity.getCourse().getNameArabic() : null)
                .courseNameEnglish(entity.getCourse() != null ? entity.getCourse().getNameEnglish() : null)

                .isRoot(entity.getIsRoot())
                .isSystemGenerated(entity.getIsSystemGenerated())
                .isDeleted(entity.getIsDeleted())
                .isArchived(entity.getIsArchived())
                .showLinkedNotes(entity.getShowLinkedNotes())
                .showLinkedTasks(entity.getShowLinkedTasks())

                .childFolderCount(childFolderCount)
                .itemCount(itemCount)

                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PersonalResourceFolderResponse toResponse(PersonalResourceFolder entity) {
        return toResponse(entity, 0, 0);
    }
}
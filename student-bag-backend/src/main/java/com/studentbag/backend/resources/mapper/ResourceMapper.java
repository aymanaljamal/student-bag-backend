package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.resources.dto.request.AdminResourceRequest;
import com.studentbag.backend.resources.dto.request.LearningObjectRequest;
import com.studentbag.backend.resources.dto.request.PersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.PersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.response.AdminResourceResponse;
import com.studentbag.backend.resources.dto.response.LearningObjectResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceItemResponse;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.LearningObject;
import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import com.studentbag.backend.student.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public LearningObject toLearningObjectEntity(LearningObjectRequest request) {
        LearningObject entity = new LearningObject();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setKeywords(request.getKeywords());
        entity.setLanguage(request.getLanguage());
        entity.setFormat(request.getFormat());
        entity.setDifficulty(request.getDifficulty());
        entity.setIntendedEndUserRole(request.getIntendedEndUserRole());
        entity.setEducationalLevel(request.getEducationalLevel());
        entity.setResourceType(request.getResourceType());
        entity.setTypicalLearningTimeMinutes(request.getTypicalLearningTimeMinutes());
        entity.setUrl(request.getUrl());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        entity.setCreatedByUserId(request.getCreatedByUserId());
        return entity;
    }

    public void updateLearningObjectEntity(LearningObject entity, LearningObjectRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setKeywords(request.getKeywords());
        entity.setLanguage(request.getLanguage());
        entity.setFormat(request.getFormat());
        entity.setDifficulty(request.getDifficulty());
        entity.setIntendedEndUserRole(request.getIntendedEndUserRole());
        entity.setEducationalLevel(request.getEducationalLevel());
        entity.setResourceType(request.getResourceType());
        entity.setTypicalLearningTimeMinutes(request.getTypicalLearningTimeMinutes());
        entity.setUrl(request.getUrl());
        entity.setThumbnailUrl(request.getThumbnailUrl());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : entity.getIsActive());
        entity.setCreatedByUserId(request.getCreatedByUserId());
    }

    public LearningObjectResponse toLearningObjectResponse(LearningObject entity) {
        return LearningObjectResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .keywords(entity.getKeywords())
                .language(entity.getLanguage())
                .format(entity.getFormat())
                .difficulty(entity.getDifficulty())
                .intendedEndUserRole(entity.getIntendedEndUserRole())
                .educationalLevel(entity.getEducationalLevel())
                .resourceType(entity.getResourceType())
                .typicalLearningTimeMinutes(entity.getTypicalLearningTimeMinutes())
                .url(entity.getUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .isActive(entity.getIsActive())
                .createdByUserId(entity.getCreatedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public AdminResource toAdminResourceEntity(
            AdminResourceRequest request,
            LearningObject learningObject,
            Institution institution,
            Term term,
            Course course
    ) {
        AdminResource entity = new AdminResource();
        entity.setLearningObject(learningObject);
        entity.setInstitution(institution);
        entity.setTerm(term);
        entity.setCourse(course);
        entity.setGradeOrLevel(request.getGradeOrLevel());
        entity.setVisibilityScope(request.getVisibilityScope());
        entity.setVersion(request.getVersion() != null ? request.getVersion() : 1);
        entity.setIsApproved(request.getIsApproved() != null ? request.getIsApproved() : false);
        entity.setApprovedByAdminId(request.getApprovedByAdminId());
        return entity;
    }

    public void updateAdminResourceEntity(
            AdminResource entity,
            AdminResourceRequest request,
            LearningObject learningObject,
            Institution institution,
            Term term,
            Course course
    ) {
        entity.setLearningObject(learningObject);
        entity.setInstitution(institution);
        entity.setTerm(term);
        entity.setCourse(course);
        entity.setGradeOrLevel(request.getGradeOrLevel());
        entity.setVisibilityScope(request.getVisibilityScope());
        entity.setVersion(request.getVersion() != null ? request.getVersion() : entity.getVersion());
        entity.setIsApproved(request.getIsApproved() != null ? request.getIsApproved() : entity.getIsApproved());
        entity.setApprovedByAdminId(request.getApprovedByAdminId());
    }

    public AdminResourceResponse toAdminResourceResponse(AdminResource entity) {
        return AdminResourceResponse.builder()
                .id(entity.getId())
                .learningObjectId(entity.getLearningObject() != null ? entity.getLearningObject().getId() : null)
                .institutionId(entity.getInstitution() != null ? entity.getInstitution().getId() : null)
                .termId(entity.getTerm() != null ? entity.getTerm().getId() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .gradeOrLevel(entity.getGradeOrLevel())
                .visibilityScope(entity.getVisibilityScope())
                .version(entity.getVersion())
                .isApproved(entity.getIsApproved())
                .approvedByAdminId(entity.getApprovedByAdminId())
                .approvedAt(entity.getApprovedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PersonalResourceFolder toFolderEntity(
            PersonalResourceFolderRequest request,
            Student student,
            PersonalResourceFolder parentFolder,
            Course course
    ) {
        PersonalResourceFolder entity = new PersonalResourceFolder();
        entity.setOwnerStudent(student);
        entity.setParentFolder(parentFolder);
        entity.setName(request.getName());
        entity.setPath(request.getPath());
        entity.setCourse(course);
        entity.setIsAutoGenerated(request.getIsAutoGenerated() != null ? request.getIsAutoGenerated() : false);
        return entity;
    }

    public void updateFolderEntity(
            PersonalResourceFolder entity,
            PersonalResourceFolderRequest request,
            Student student,
            PersonalResourceFolder parentFolder,
            Course course
    ) {
        entity.setOwnerStudent(student);
        entity.setParentFolder(parentFolder);
        entity.setName(request.getName());
        entity.setPath(request.getPath());
        entity.setCourse(course);
        entity.setIsAutoGenerated(request.getIsAutoGenerated() != null ? request.getIsAutoGenerated() : entity.getIsAutoGenerated());
    }

    public PersonalResourceFolderResponse toFolderResponse(PersonalResourceFolder entity) {
        return PersonalResourceFolderResponse.builder()
                .id(entity.getId())
                .ownerStudentId(entity.getOwnerStudent() != null ? entity.getOwnerStudent().getId() : null)
                .parentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null)
                .name(entity.getName())
                .path(entity.getPath())
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .isAutoGenerated(entity.getIsAutoGenerated())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PersonalResourceItem toItemEntity(
            PersonalResourceItemRequest request,
            Student student,
            PersonalResourceFolder folder,
            AdminResource linkedAdminResource,
            Note note
    ) {
        PersonalResourceItem entity = new PersonalResourceItem();
        entity.setOwnerStudent(student);
        entity.setFolder(folder);
        entity.setTitle(request.getTitle());
        entity.setFormat(request.getFormat());
        entity.setIsExamPreparation(request.getIsExamPreparation() != null ? request.getIsExamPreparation() : false);
        entity.setIsImportant(request.getIsImportant() != null ? request.getIsImportant() : false);
        entity.setLinkedAdminResource(linkedAdminResource);
        entity.setNote(note);
        entity.setFileUrl(request.getFileUrl());
        return entity;
    }

    public void updateItemEntity(
            PersonalResourceItem entity,
            PersonalResourceItemRequest request,
            Student student,
            PersonalResourceFolder folder,
            AdminResource linkedAdminResource,
            Note note
    ) {
        entity.setOwnerStudent(student);
        entity.setFolder(folder);
        entity.setTitle(request.getTitle());
        entity.setFormat(request.getFormat());
        entity.setIsExamPreparation(request.getIsExamPreparation() != null ? request.getIsExamPreparation() : entity.getIsExamPreparation());
        entity.setIsImportant(request.getIsImportant() != null ? request.getIsImportant() : entity.getIsImportant());
        entity.setLinkedAdminResource(linkedAdminResource);
        entity.setNote(note);
        entity.setFileUrl(request.getFileUrl());
    }

    public PersonalResourceItemResponse toItemResponse(PersonalResourceItem entity) {
        return PersonalResourceItemResponse.builder()
                .id(entity.getId())
                .ownerStudentId(entity.getOwnerStudent() != null ? entity.getOwnerStudent().getId() : null)
                .folderId(entity.getFolder() != null ? entity.getFolder().getId() : null)
                .title(entity.getTitle())
                .format(entity.getFormat())
                .isExamPreparation(entity.getIsExamPreparation())
                .isImportant(entity.getIsImportant())
                .linkedAdminResourceId(entity.getLinkedAdminResource() != null ? entity.getLinkedAdminResource().getId() : null)
                .noteId(entity.getNote() != null ? entity.getNote().getId() : null)
                .fileUrl(entity.getFileUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
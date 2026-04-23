package com.studentbag.backend.resources.repository;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.resources.entity.PersonalResourceItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalResourceItemRepository extends JpaRepository<PersonalResourceItem, Long> {

    List<PersonalResourceItem> findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId
    );

    List<PersonalResourceItem> findByStudentIdAndFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            Long folderId
    );

    List<PersonalResourceItem> findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            Long courseId
    );

    List<PersonalResourceItem> findByStudentIdAndCategoryAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            ResourceCategory category
    );

    List<PersonalResourceItem> findByStudentIdAndResourceTypeAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            ResourceType resourceType
    );

    Optional<PersonalResourceItem> findByIdAndStudentIdAndIsDeletedFalse(
            Long id,
            Long studentId
    );

    List<PersonalResourceItem> findByStudentIdAndCopiedFromAdminResourceIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long studentId,
            Long copiedFromAdminResourceId
    );

    List<PersonalResourceItem> findByStudentIdAndLinkedNoteIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            Long linkedNoteId
    );

    List<PersonalResourceItem> findByStudentIdAndLinkedTaskIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId,
            Long linkedTaskId
    );
}
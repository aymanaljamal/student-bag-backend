package com.studentbag.backend.resources.repository;

import com.studentbag.backend.resources.entity.PersonalResourceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalResourceItemRepository extends JpaRepository<PersonalResourceItem, Long> {

    List<PersonalResourceItem> findByOwnerStudentId(Long ownerStudentId);

    List<PersonalResourceItem> findByFolderId(Long folderId);

    List<PersonalResourceItem> findByOwnerStudentIdAndIsImportantTrue(Long ownerStudentId);

    List<PersonalResourceItem> findByOwnerStudentIdAndIsExamPreparationTrue(Long ownerStudentId);

    List<PersonalResourceItem> findByLinkedAdminResourceId(Long linkedAdminResourceId);

    List<PersonalResourceItem> findByNoteId(Long noteId);

    List<PersonalResourceItem> findByOwnerStudentIdAndTitleContainingIgnoreCase(Long ownerStudentId, String title);
}
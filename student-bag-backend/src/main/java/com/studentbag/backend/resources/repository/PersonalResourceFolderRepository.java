package com.studentbag.backend.resources.repository;

import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalResourceFolderRepository extends JpaRepository<PersonalResourceFolder, Long> {

    List<PersonalResourceFolder> findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long studentId
    );

    List<PersonalResourceFolder> findByStudentIdAndParentFolderIsNullAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
            Long studentId
    );

    List<PersonalResourceFolder> findByStudentIdAndParentFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
            Long studentId,
            Long parentFolderId
    );

    List<PersonalResourceFolder> findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
            Long studentId,
            Long courseId
    );

    Optional<PersonalResourceFolder> findByStudentIdAndIsRootTrueAndIsDeletedFalse(
            Long studentId
    );

    Optional<PersonalResourceFolder> findByIdAndStudentIdAndIsDeletedFalse(
            Long id,
            Long studentId
    );

    boolean existsByStudentIdAndParentFolderIdAndNameIgnoreCaseAndIsDeletedFalse(
            Long studentId,
            Long parentFolderId,
            String name
    );

    boolean existsByStudentIdAndParentFolderIsNullAndNameIgnoreCaseAndIsDeletedFalse(
            Long studentId,
            String name
    );

    List<PersonalResourceFolder> findByStudentIdAndIsSystemGeneratedTrueAndIsDeletedFalseOrderByCreatedAtAsc(
            Long studentId
    );
}
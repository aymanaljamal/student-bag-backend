package com.studentbag.backend.resources.repository;

import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalResourceFolderRepository extends JpaRepository<PersonalResourceFolder, Long> {

    List<PersonalResourceFolder> findByOwnerStudentId(Long ownerStudentId);

    List<PersonalResourceFolder> findByOwnerStudentIdAndParentFolderIsNull(Long ownerStudentId);

    List<PersonalResourceFolder> findByParentFolderId(Long parentFolderId);

    List<PersonalResourceFolder> findByOwnerStudentIdAndCourseId(Long ownerStudentId, Long courseId);
}
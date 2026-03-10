package com.studentbag.backend.parentlink.repository;

import com.studentbag.backend.parentlink.entity.ParentStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParentId(Long parentId);
    List<ParentStudentLink> findByStudentId(Long studentId);
}
package com.studentbag.backend.resources.repository;

import com.studentbag.backend.domain.enums.VisibilityScope;
import com.studentbag.backend.resources.entity.AdminResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminResourceRepository extends JpaRepository<AdminResource, Long> {

    List<AdminResource> findByInstitutionId(Long institutionId);

    List<AdminResource> findByCourseId(Long courseId);

    List<AdminResource> findByTermId(Long termId);

    List<AdminResource> findByInstitutionIdAndIsApprovedTrue(Long institutionId);

    List<AdminResource> findByCourseIdAndIsApprovedTrue(Long courseId);

    List<AdminResource> findByVisibilityScopeAndIsApprovedTrue(VisibilityScope visibilityScope);
}
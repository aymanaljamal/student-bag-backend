package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository for Course entity
 */
public interface CourseRepository extends JpaRepository<Course, Long>,
        JpaSpecificationExecutor<Course> {

    Optional<Course> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Course> findByCodeAndInstitution(String code, Institution institution);

}
package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    List<Course> findByInstitutionIdAndIsActiveTrue(Long institutionId);

    List<Course> findByLevelAndIsActiveTrue(com.studentbag.backend.domain.enums.AcademicLevel level);
}
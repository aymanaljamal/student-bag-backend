package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByCourseId(Long courseId);

    List<CourseSection> findByTermId(Long termId);

    List<CourseSection> findByInstructorId(Long instructorId);

    List<CourseSection> findByCourseIdAndTermId(Long courseId, Long termId);
}
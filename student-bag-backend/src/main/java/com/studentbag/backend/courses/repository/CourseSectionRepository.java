package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByCourseId(Long courseId);
    List<CourseSection> findByTermId(Long termId);
    List<CourseSection> findByInstructorId(Long instructorId);
    List<CourseSection> findByCourseIdAndTermId(Long courseId, Long termId);
    Optional<CourseSection> findByExternalId(String externalId);
    Optional<CourseSection> findByCourseAndTermAndSectionNumberAndSectionType(
            Course course,
            Term term,
            String sectionNumber,
            SectionType sectionType
    );
    List<CourseSection> findByCourseAndTerm(Course course, Term term);
}
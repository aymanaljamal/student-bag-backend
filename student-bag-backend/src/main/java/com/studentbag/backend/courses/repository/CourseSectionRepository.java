package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.courses.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findByCourseIdInAndTermId(List<Long> courseIds, Long termId);
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
    @Query("SELECT DISTINCT cs FROM CourseSection cs " +
            "JOIN FETCH cs.classSessions " +
            "WHERE cs.course.id IN :courseIds AND cs.term.id = :termId")
    List<CourseSection> findAllWithSessions(List<Long> courseIds, Long termId);

}
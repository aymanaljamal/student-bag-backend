package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByCourseSectionId(Long courseSectionId);
    List<ClassSession> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<ClassSession> findByCourseSection(CourseSection courseSection);
    void deleteByCourseSection(CourseSection courseSection);
    List<ClassSession> findByCourseSectionTermId(Long termId);
}
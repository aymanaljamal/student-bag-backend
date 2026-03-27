package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.StudentCourseDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCourseDifficultyRepository extends JpaRepository<StudentCourseDifficulty, Long> {

    Optional<StudentCourseDifficulty> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<StudentCourseDifficulty> findByStudentId(Long studentId);

    List<StudentCourseDifficulty> findByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
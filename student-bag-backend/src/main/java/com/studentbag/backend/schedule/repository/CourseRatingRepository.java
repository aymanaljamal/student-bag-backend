package com.studentbag.backend.schedule.repository;
import com.studentbag.backend.schedule.entity.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {
    Optional<CourseRating> findByStudentIdAndCourseId(Long studentId, Long courseId);
    @Query("SELECT AVG(cr.difficultyRating) FROM CourseRating cr WHERE cr.course.id = :courseId")
    Double getAverageDifficulty(Long courseId);
    List<CourseRating> findByStudentId(Long studentId);
}
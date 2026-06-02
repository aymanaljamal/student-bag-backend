package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.schedule.dto.request.CourseRatingRequestDTO;
import com.studentbag.backend.schedule.entity.CourseRating;
import com.studentbag.backend.schedule.repository.CourseRatingRepository;
import com.studentbag.backend.schedule.service.CourseRatingService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseRatingServiceImpl implements CourseRatingService {

    private final CourseRatingRepository courseRatingRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public void saveRatings(Long studentId, List<CourseRatingRequestDTO> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return;
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        for (CourseRatingRequestDTO dto : ratings) {
            if (dto == null || dto.getCourseId() == null || dto.getDifficultyRating() == null) {
                continue;
            }

            validateDifficultyRating(dto.getDifficultyRating());

            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found: " + dto.getCourseId()));

            CourseRating rating = courseRatingRepository
                    .findByStudentIdAndCourseId(studentId, dto.getCourseId())
                    .orElseGet(() ->
                            CourseRating.builder()
                                    .student(student)
                                    .course(course)
                                    .build()
                    );

            rating.setDifficultyRating(dto.getDifficultyRating());

            courseRatingRepository.save(rating);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageDifficulty(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course id is required");
        }

        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }

        Double average = courseRatingRepository.getAverageDifficulty(courseId);

        return average != null ? average : 0.0;
    }

    private void validateDifficultyRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Difficulty rating must be between 1 and 5");
        }
    }
}
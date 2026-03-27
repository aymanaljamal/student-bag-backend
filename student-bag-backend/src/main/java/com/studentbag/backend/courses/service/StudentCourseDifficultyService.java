package com.studentbag.backend.courses.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.StudentCourseDifficulty;
import com.studentbag.backend.courses.repository.StudentCourseDifficultyRepository;
import com.studentbag.backend.domain.enums.CourseDifficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCourseDifficultyService {

    private final StudentCourseDifficultyRepository repository;

    public StudentCourseDifficulty create(StudentCourseDifficulty entity) {
        boolean exists = repository.existsByStudentIdAndCourseId(
                entity.getStudent().getId(),
                entity.getCourse().getId()
        );

        if (exists) {
            throw new IllegalArgumentException("Student already rated this course.");
        }

        return repository.save(entity);
    }

    public StudentCourseDifficulty update(Long id, CourseDifficulty difficulty, String note) {
        StudentCourseDifficulty existing = getById(id);
        existing.setDifficulty(difficulty);
        existing.setNote(note);
        return repository.save(existing);
    }

    public StudentCourseDifficulty getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student course difficulty not found with id: " + id));
    }

    public StudentCourseDifficulty getByStudentAndCourse(Long studentId, Long courseId) {
        return repository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Difficulty rating not found for student " + studentId + " and course " + courseId
                ));
    }

    public List<StudentCourseDifficulty> getByStudent(Long studentId) {
        return repository.findByStudentId(studentId);
    }

    public List<StudentCourseDifficulty> getByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    public void delete(Long id) {
        StudentCourseDifficulty entity = getById(id);
        repository.delete(entity);
    }
}
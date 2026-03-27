package com.studentbag.backend.courses.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.domain.enums.AcademicLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public Course create(Course course) {
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException("Course code already exists: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    public Course update(Long id, Course updatedCourse) {
        Course existing = getById(id);

        existing.setCode(updatedCourse.getCode());
        existing.setName(updatedCourse.getName());
        existing.setDescription(updatedCourse.getDescription());
        existing.setCreditHours(updatedCourse.getCreditHours());
        existing.setLevel(updatedCourse.getLevel());
        existing.setInstitution(updatedCourse.getInstitution());
        existing.setIsActive(updatedCourse.getIsActive());

        return courseRepository.save(existing);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    public Course getByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + code));
    }

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public List<Course> getActiveByInstitution(Long institutionId) {
        return courseRepository.findByInstitutionIdAndIsActiveTrue(institutionId);
    }

    public List<Course> getByLevel(AcademicLevel level) {
        return courseRepository.findByLevelAndIsActiveTrue(level);
    }

    public void delete(Long id) {
        Course course = getById(id);
        courseRepository.delete(course);
    }

    public void deactivate(Long id) {
        Course course = getById(id);
        course.setIsActive(false);
        courseRepository.save(course);
    }

    public void activate(Long id) {
        Course course = getById(id);
        course.setIsActive(true);
        courseRepository.save(course);
    }
}
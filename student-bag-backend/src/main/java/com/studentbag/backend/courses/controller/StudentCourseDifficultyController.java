package com.studentbag.backend.courses.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.dto.request.StudentCourseDifficultyRequest;
import com.studentbag.backend.courses.dto.response.StudentCourseDifficultyResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.StudentCourseDifficulty;
import com.studentbag.backend.courses.mapper.StudentCourseDifficultyMapper;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.service.StudentCourseDifficultyService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-difficulties")
@RequiredArgsConstructor
public class StudentCourseDifficultyController {

    private final StudentCourseDifficultyService difficultyService;
    private final CourseService courseService;
    private final StudentRepository studentRepository;
    private final StudentCourseDifficultyMapper difficultyMapper;

    @PostMapping
    public StudentCourseDifficultyResponse create(@Valid @RequestBody StudentCourseDifficultyRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()
                ));

        Course course = courseService.getById(request.getCourseId());

        StudentCourseDifficulty entity = difficultyMapper.toEntity(request, student, course);
        return difficultyMapper.toResponse(difficultyService.create(entity));
    }

    @PutMapping("/{id}")
    public StudentCourseDifficultyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StudentCourseDifficultyRequest request
    ) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()
                ));

        Course course = courseService.getById(request.getCourseId());

        StudentCourseDifficulty existing = difficultyService.getById(id);
        difficultyMapper.updateEntity(existing, request, student, course);

        return difficultyMapper.toResponse(
                difficultyService.update(id, existing.getDifficulty(), existing.getNote())
        );
    }

    @GetMapping("/{id}")
    public StudentCourseDifficultyResponse getById(@PathVariable Long id) {
        return difficultyMapper.toResponse(difficultyService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public List<StudentCourseDifficultyResponse> getByStudent(@PathVariable Long studentId) {
        return difficultyService.getByStudent(studentId)
                .stream()
                .map(difficultyMapper::toResponse)
                .toList();
    }

    @GetMapping("/course/{courseId}")
    public List<StudentCourseDifficultyResponse> getByCourse(@PathVariable Long courseId) {
        return difficultyService.getByCourse(courseId)
                .stream()
                .map(difficultyMapper::toResponse)
                .toList();
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public StudentCourseDifficultyResponse getByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId
    ) {
        return difficultyMapper.toResponse(
                difficultyService.getByStudentAndCourse(studentId, courseId)
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        difficultyService.delete(id);
    }
}
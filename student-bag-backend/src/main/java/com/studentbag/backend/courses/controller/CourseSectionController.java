package com.studentbag.backend.courses.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.dto.request.CourseSectionRequest;
import com.studentbag.backend.courses.dto.response.CourseSectionResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.mapper.CourseSectionMapper;
import com.studentbag.backend.courses.service.CourseSectionService;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.service.TermService;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-sections")
@RequiredArgsConstructor
public class CourseSectionController {

    private final CourseSectionService courseSectionService;
    private final CourseService courseService;
    private final TermService termService;
    private final CourseSectionMapper courseSectionMapper;
    private final InstructorRepository instructorRepository;

    @PostMapping
    public CourseSectionResponse create(@Valid @RequestBody CourseSectionRequest request) {
        Course course = courseService.getById(request.getCourseId());
        Term term = termService.getById(request.getTermId());

        Instructor instructor = null;
        if (request.getInstructorId() != null) {
            instructor = instructorRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Instructor not found with id: " + request.getInstructorId()
                    ));
        }

        CourseSection section = courseSectionMapper.toEntity(request, course, term, instructor);
        return courseSectionMapper.toResponse(courseSectionService.create(section));
    }

    @PutMapping("/{id}")
    public CourseSectionResponse update(@PathVariable Long id, @Valid @RequestBody CourseSectionRequest request) {
        Course course = courseService.getById(request.getCourseId());
        Term term = termService.getById(request.getTermId());

        Instructor instructor = null;
        if (request.getInstructorId() != null) {
            instructor = instructorRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Instructor not found with id: " + request.getInstructorId()
                    ));
        }

        CourseSection existing = courseSectionService.getById(id);
        courseSectionMapper.updateEntity(existing, request, course, term, instructor);

        return courseSectionMapper.toResponse(courseSectionService.update(id, existing));
    }

    @GetMapping("/{id}")
    public CourseSectionResponse getById(@PathVariable Long id) {
        return courseSectionMapper.toResponse(courseSectionService.getById(id));
    }

    @GetMapping
    public List<CourseSectionResponse> getAll() {
        return courseSectionService.getAll()
                .stream()
                .map(courseSectionMapper::toResponse)
                .toList();
    }

    @GetMapping("/course/{courseId}")
    public List<CourseSectionResponse> getByCourse(@PathVariable Long courseId) {
        return courseSectionService.getByCourse(courseId)
                .stream()
                .map(courseSectionMapper::toResponse)
                .toList();
    }

    @GetMapping("/term/{termId}")
    public List<CourseSectionResponse> getByTerm(@PathVariable Long termId) {
        return courseSectionService.getByTerm(termId)
                .stream()
                .map(courseSectionMapper::toResponse)
                .toList();
    }

    @GetMapping("/instructor/{instructorId}")
    public List<CourseSectionResponse> getByInstructor(@PathVariable Long instructorId) {
        return courseSectionService.getByInstructor(instructorId)
                .stream()
                .map(courseSectionMapper::toResponse)
                .toList();
    }

    @GetMapping("/course/{courseId}/term/{termId}")
    public List<CourseSectionResponse> getByCourseAndTerm(
            @PathVariable Long courseId,
            @PathVariable Long termId
    ) {
        return courseSectionService.getByCourseAndTerm(courseId, termId)
                .stream()
                .map(courseSectionMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/enroll")
    public CourseSectionResponse enrollStudent(@PathVariable Long id) {
        return courseSectionMapper.toResponse(courseSectionService.enrollStudent(id));
    }

    @PatchMapping("/{id}/drop")
    public CourseSectionResponse dropStudent(@PathVariable Long id) {
        return courseSectionMapper.toResponse(courseSectionService.dropStudent(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseSectionService.delete(id);
    }
}
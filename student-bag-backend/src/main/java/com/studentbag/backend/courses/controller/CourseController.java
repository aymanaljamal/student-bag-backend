package com.studentbag.backend.courses.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.dto.request.CourseRequest;
import com.studentbag.backend.courses.dto.response.CourseResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.mapper.CourseMapper;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.domain.enums.AcademicLevel;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;
    private final InstitutionRepository institutionRepository;

    @PostMapping
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Course course = courseMapper.toEntity(request, institution);
        return courseMapper.toResponse(courseService.create(course));
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Course existing = courseService.getById(id);
        courseMapper.updateEntity(existing, request, institution);

        return courseMapper.toResponse(courseService.update(id, existing));
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id) {
        return courseMapper.toResponse(courseService.getById(id));
    }

    @GetMapping("/code/{code}")
    public CourseResponse getByCode(@PathVariable String code) {
        return courseMapper.toResponse(courseService.getByCode(code));
    }

    @GetMapping
    public List<CourseResponse> getAll() {
        return courseService.getAll()
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @GetMapping("/institution/{institutionId}")
    public List<CourseResponse> getByInstitution(@PathVariable Long institutionId) {
        return courseService.getActiveByInstitution(institutionId)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @GetMapping("/level/{level}")
    public List<CourseResponse> getByLevel(@PathVariable AcademicLevel level) {
        return courseService.getByLevel(level)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/activate")
    public CourseResponse activate(@PathVariable Long id) {
        courseService.activate(id);
        return courseMapper.toResponse(courseService.getById(id));
    }

    @PatchMapping("/{id}/deactivate")
    public CourseResponse deactivate(@PathVariable Long id) {
        courseService.deactivate(id);
        return courseMapper.toResponse(courseService.getById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }
}
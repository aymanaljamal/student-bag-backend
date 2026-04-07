package com.studentbag.backend.courses.controller;

import com.studentbag.backend.courses.dto.request.CourseRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseResponseDTO;
import com.studentbag.backend.courses.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Courses
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public CourseResponseDTO create(@Valid @RequestBody CourseRequestDTO request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    public CourseResponseDTO update(@PathVariable Long id, @Valid @RequestBody CourseRequestDTO request) {
        return courseService.update(id, request);
    }

    @GetMapping("/{id}")
    public CourseResponseDTO getById(@PathVariable Long id) {
        return courseService.getById(id, true); // true لإظهار الأقسام
    }


    @GetMapping("/all")
    public List<CourseResponseDTO> getAll(
            @RequestParam(defaultValue = "false") boolean includeSections
    ) {
        return courseService.getAll(includeSections);
    }

    @GetMapping("/search")
    public Page<CourseResponseDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeSections,
            Pageable pageable
    ) {
        return courseService.search(keyword, institutionId, level, isActive, includeSections, pageable);
    }
}
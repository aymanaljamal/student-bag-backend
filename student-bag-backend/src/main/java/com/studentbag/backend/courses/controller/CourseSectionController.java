package com.studentbag.backend.courses.controller;

import com.studentbag.backend.courses.dto.request.CourseSectionRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseSectionResponseDTO;
import com.studentbag.backend.courses.service.CourseSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-sections")
@RequiredArgsConstructor
public class CourseSectionController {

    private final CourseSectionService service;

    @PostMapping
    public CourseSectionResponseDTO create(@Valid @RequestBody CourseSectionRequestDTO request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CourseSectionResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CourseSectionRequestDTO request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    public CourseSectionResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CourseSectionResponseDTO> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
package com.studentbag.backend.courses.controller;

import com.studentbag.backend.courses.dto.request.ClassSessionRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import com.studentbag.backend.courses.service.ClassSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Class Sessions
 */
@RestController
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    /**
     * Create new class session
     */
    @PostMapping
    public ClassSessionResponseDTO create(@Valid @RequestBody ClassSessionRequestDTO request) {
        return classSessionService.create(request);
    }

    /**
     * Update class session
     */
    @PutMapping("/{id}")
    public ClassSessionResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ClassSessionRequestDTO request
    ) {
        return classSessionService.update(id, request);
    }

    /**
     * Get class session by ID
     */
    @GetMapping("/{id}")
    public ClassSessionResponseDTO getById(@PathVariable Long id) {
        return classSessionService.getById(id);
    }

    /**
     * Get all class sessions
     */
    @GetMapping
    public List<ClassSessionResponseDTO> getAll() {
        return classSessionService.getAll();
    }

    /**
     * Delete class session
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        classSessionService.delete(id);
    }
}
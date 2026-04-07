package com.studentbag.backend.courses.controller;

import com.studentbag.backend.courses.dto.request.FacultyRequestDTO;
import com.studentbag.backend.courses.dto.response.FacultyResponseDTO;
import com.studentbag.backend.courses.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    public FacultyResponseDTO create(@Valid @RequestBody FacultyRequestDTO request) {
        return facultyService.create(request);
    }

    @PutMapping("/{id}")
    public FacultyResponseDTO update(@PathVariable Long id, @Valid @RequestBody FacultyRequestDTO request) {
        return facultyService.update(id, request);
    }

    @GetMapping("/{id}")
    public FacultyResponseDTO getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @GetMapping("/all")
    public List<FacultyResponseDTO> getAll() {
        return facultyService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        facultyService.delete(id);
    }

    @GetMapping("/search")
    public Page<FacultyResponseDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        return facultyService.search(keyword, institutionId, isActive, pageable);
    }
}
package com.studentbag.backend.courses.controller;

import com.studentbag.backend.courses.dto.request.DepartmentRequestDTO;
import com.studentbag.backend.courses.dto.response.DepartmentResponseDTO;
import com.studentbag.backend.courses.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public DepartmentResponseDTO create(
            @Valid @RequestBody DepartmentRequestDTO request
    ) {
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    public DepartmentResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO request
    ) {
        return departmentService.update(id, request);
    }

    @GetMapping("/{id}")
    public DepartmentResponseDTO getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @GetMapping("/all")
    public List<DepartmentResponseDTO> getAll(
            @RequestParam(required = false) Long institutionId
    ) {
        if (institutionId != null) {
            return departmentService.getAllByInstitution(institutionId);
        }

        return departmentService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }

    @GetMapping("/search")
    public Page<DepartmentResponseDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        return departmentService.search(
                keyword,
                institutionId,
                facultyId,
                isActive,
                pageable
        );
    }
}
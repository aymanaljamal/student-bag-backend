package com.studentbag.backend.institution.controller;

import com.studentbag.backend.institution.dto.request.InstitutionRequest;
import com.studentbag.backend.institution.dto.response.InstitutionResponse;
import com.studentbag.backend.institution.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping("/active")
    public List<InstitutionResponse> getActiveInstitutions() {
        return institutionService.getActiveInstitutions();
    }

    @GetMapping
    public List<InstitutionResponse> getAllInstitutions() {
        return institutionService.getAllInstitutions();
    }

    @GetMapping("/{id}")
    public InstitutionResponse getInstitutionById(@PathVariable Long id) {
        return institutionService.getInstitutionById(id);
    }

    @PostMapping
    public InstitutionResponse createInstitution(
            @Valid @RequestBody InstitutionRequest request
    ) {
        return institutionService.createInstitution(request);
    }

    @PutMapping("/{id}")
    public InstitutionResponse updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionRequest request
    ) {
        return institutionService.updateInstitution(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteInstitution(@PathVariable Long id) {
        institutionService.deleteInstitution(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public InstitutionResponse toggleInstitutionStatus(@PathVariable Long id) {
        return institutionService.toggleInstitutionStatus(id);
    }
}
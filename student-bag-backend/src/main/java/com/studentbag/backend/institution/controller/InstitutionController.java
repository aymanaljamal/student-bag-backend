package com.studentbag.backend.institution.controller;

import com.studentbag.backend.institution.dto.response.InstitutionResponse;
import com.studentbag.backend.institution.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
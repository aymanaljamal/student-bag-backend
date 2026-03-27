package com.studentbag.backend.courses.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.dto.request.TermRequest;
import com.studentbag.backend.courses.dto.response.TermResponse;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.mapper.TermMapper;
import com.studentbag.backend.courses.service.TermService;
import com.studentbag.backend.domain.enums.Season;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;
    private final TermMapper termMapper;
    private final InstitutionRepository institutionRepository;

    @PostMapping
    public TermResponse create(@Valid @RequestBody TermRequest request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Term term = termMapper.toEntity(request, institution);
        return termMapper.toResponse(termService.create(term));
    }

    @PutMapping("/{id}")
    public TermResponse update(@PathVariable Long id, @Valid @RequestBody TermRequest request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Term existing = termService.getById(id);
        termMapper.updateEntity(existing, request, institution);

        return termMapper.toResponse(termService.update(id, existing));
    }

    @GetMapping("/{id}")
    public TermResponse getById(@PathVariable Long id) {
        return termMapper.toResponse(termService.getById(id));
    }

    @GetMapping
    public List<TermResponse> getAll() {
        return termService.getAll()
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @GetMapping("/institution/{institutionId}")
    public List<TermResponse> getByInstitution(@PathVariable Long institutionId) {
        return termService.getByInstitution(institutionId)
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @GetMapping("/institution/{institutionId}/season/{season}")
    public List<TermResponse> getByInstitutionAndSeason(
            @PathVariable Long institutionId,
            @PathVariable Season season
    ) {
        return termService.getByInstitutionAndSeason(institutionId, season)
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @GetMapping("/active")
    public List<TermResponse> getActiveTerms(@RequestParam LocalDate date) {
        return termService.getActiveTerms(date)
                .stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        termService.delete(id);
    }
}
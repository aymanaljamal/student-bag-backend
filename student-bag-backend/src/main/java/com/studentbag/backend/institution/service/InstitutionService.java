package com.studentbag.backend.institution.service;

import com.studentbag.backend.institution.dto.request.InstitutionRequest;
import com.studentbag.backend.institution.dto.response.InstitutionResponse;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public List<InstitutionResponse> getActiveInstitutions() {
        return institutionRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InstitutionResponse> getAllInstitutions() {
        return institutionRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InstitutionResponse getInstitutionById(Long id) {
        Institution institution = findInstitution(id);
        return toResponse(institution);
    }

    public InstitutionResponse createInstitution(InstitutionRequest request) {
        if (institutionRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Institution name already exists");
        }

        Institution institution = new Institution();
        applyRequest(institution, request);

        if (request.getActive() == null) {
            institution.setActive(true);
        }

        return toResponse(institutionRepository.save(institution));
    }

    public InstitutionResponse updateInstitution(Long id, InstitutionRequest request) {
        Institution institution = findInstitution(id);

        if (institutionRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Institution name already exists");
        }

        applyRequest(institution, request);

        return toResponse(institutionRepository.save(institution));
    }

    public void deleteInstitution(Long id) {
        Institution institution = findInstitution(id);
        institutionRepository.delete(institution);
    }

    public InstitutionResponse toggleInstitutionStatus(Long id) {
        Institution institution = findInstitution(id);
        institution.setActive(!Boolean.TRUE.equals(institution.getActive()));
        return toResponse(institutionRepository.save(institution));
    }

    private Institution findInstitution(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));
    }

    private void applyRequest(Institution institution, InstitutionRequest request) {
        institution.setName(request.getName());
        institution.setType(request.getType());
        institution.setCountry(request.getCountry());
        institution.setCity(request.getCity());
        institution.setWebsite(request.getWebsite());

        if (request.getActive() != null) {
            institution.setActive(request.getActive());
        }
    }

    private InstitutionResponse toResponse(Institution i) {
        return InstitutionResponse.builder()
                .id(i.getId())
                .name(i.getName())
                .type(i.getType().name())
                .country(i.getCountry())
                .city(i.getCity())
                .website(i.getWebsite())
                .active(i.getActive())
                .build();
    }
}
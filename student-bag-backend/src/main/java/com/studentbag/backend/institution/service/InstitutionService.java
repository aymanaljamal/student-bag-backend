package com.studentbag.backend.institution.service;

import com.studentbag.backend.institution.dto.response.InstitutionResponse;
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
                .map(i -> InstitutionResponse.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .type(i.getType().name())
                        .country(i.getCountry())
                        .city(i.getCity())
                        .website(i.getWebsite())
                        .build())
                .toList();
    }
}
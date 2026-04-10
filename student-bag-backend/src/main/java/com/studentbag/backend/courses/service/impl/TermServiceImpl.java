package com.studentbag.backend.courses.service.impl;
import com.studentbag.backend.courses.dto.response.TermResponseDTO;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.courses.service.TermService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermServiceImpl implements TermService {

    private final TermRepository termRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TermResponseDTO> getTermsByInstitution(Long institutionId) {
        log.info("Fetching all terms for institution ID: {}", institutionId);

        return termRepository.findAllByInstitutionIdOrderByStartDateDesc(institutionId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TermResponseDTO getCurrentTerm(Long institutionId) {
        log.info("Fetching current term for institution ID: {}", institutionId);

        return termRepository.findByInstitutionIdAndIsCurrentTrue(institutionId)
                .stream()
                .findFirst()
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Current term not found for this institution"));
    }

    /**
     * تحويل Entity إلى DTO (يمكنك مستقبلاً استخدام MapStruct)
     */
    private TermResponseDTO mapToDTO(Term term) {
        return TermResponseDTO.builder()
                .id(term.getId())
                .termCode(term.getTermCode())
                .name(term.getName())
                .academicYear(term.getAcademicYear())
                .season(term.getSeason())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .isCurrent(term.getIsCurrent())
                .build();
    }
}
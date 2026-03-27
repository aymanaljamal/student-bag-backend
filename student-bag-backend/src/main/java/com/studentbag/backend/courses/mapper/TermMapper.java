package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.TermRequest;
import com.studentbag.backend.courses.dto.response.TermResponse;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class TermMapper {

    public Term toEntity(TermRequest request, Institution institution) {
        Term term = new Term();
        term.setName(request.getName());
        term.setAcademicYear(request.getAcademicYear());
        term.setSeason(request.getSeason());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setInstitution(institution);
        return term;
    }

    public void updateEntity(Term term, TermRequest request, Institution institution) {
        term.setName(request.getName());
        term.setAcademicYear(request.getAcademicYear());
        term.setSeason(request.getSeason());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setInstitution(institution);
    }

    public TermResponse toResponse(Term term) {
        return TermResponse.builder()
                .id(term.getId())
                .name(term.getName())
                .academicYear(term.getAcademicYear())
                .season(term.getSeason())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .institutionId(term.getInstitution() != null ? term.getInstitution().getId() : null)
                .createdAt(term.getCreatedAt())
                .updatedAt(term.getUpdatedAt())
                .build();
    }
}
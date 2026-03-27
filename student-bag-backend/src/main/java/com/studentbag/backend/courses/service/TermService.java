package com.studentbag.backend.courses.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.domain.enums.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    public Term create(Term term) {
        return termRepository.save(term);
    }

    public Term update(Long id, Term updatedTerm) {
        Term existing = getById(id);

        existing.setName(updatedTerm.getName());
        existing.setAcademicYear(updatedTerm.getAcademicYear());
        existing.setSeason(updatedTerm.getSeason());
        existing.setStartDate(updatedTerm.getStartDate());
        existing.setEndDate(updatedTerm.getEndDate());
        existing.setInstitution(updatedTerm.getInstitution());

        return termRepository.save(existing);
    }

    public Term getById(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found with id: " + id));
    }

    public List<Term> getAll() {
        return termRepository.findAll();
    }

    public List<Term> getByInstitution(Long institutionId) {
        return termRepository.findByInstitutionId(institutionId);
    }

    public List<Term> getByInstitutionAndSeason(Long institutionId, Season season) {
        return termRepository.findByInstitutionIdAndSeason(institutionId, season);
    }

    public List<Term> getActiveTerms(LocalDate date) {
        return termRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    public boolean isActive(Long termId, LocalDate date) {
        return getById(termId).isActive(date);
    }

    public void delete(Long id) {
        Term term = getById(id);
        termRepository.delete(term);
    }
}
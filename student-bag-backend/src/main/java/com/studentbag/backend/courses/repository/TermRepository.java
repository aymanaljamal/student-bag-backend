package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {
    Optional<Term> findByTermCodeAndInstitution(String termCode, Institution institution);
    Optional<Term> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Term> findByTermCodeAndInstitutionId(String termCode, Long institutionId);
    List<Term> findAllByInstitution(Institution institution);
    List<Term> findAllByInstitutionIdOrderByStartDateDesc(Long institutionId);
    List<Term> findByInstitutionIdAndIsCurrentTrue(Long institutionId);
    Optional<Term> findFirstByOrderByIdDesc();
}
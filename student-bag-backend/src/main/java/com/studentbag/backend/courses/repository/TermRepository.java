package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByInstitutionId(Long institutionId);

    List<Term> findByInstitutionIdAndSeason(Long institutionId, Season season);

    List<Term> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
}
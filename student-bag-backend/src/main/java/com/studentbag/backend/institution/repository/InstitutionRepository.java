package com.studentbag.backend.institution.repository;

import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    List<Institution> findByActiveTrueOrderByNameAsc();
}
package com.studentbag.backend.institution.repository;

import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    List<Institution> findByActiveTrueOrderByNameAsc();

    List<Institution> findAllByOrderByNameAsc();

    Optional<Institution> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
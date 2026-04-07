package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long>,
        JpaSpecificationExecutor<Faculty> {

    Optional<Faculty> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Faculty> findByNameArabicAndInstitution(String nameArabic, Institution institution);
}
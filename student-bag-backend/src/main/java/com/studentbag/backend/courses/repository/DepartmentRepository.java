package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {

    Optional<Department> findByExternalIdAndFaculty(String externalId, Faculty faculty);

    Optional<Department> findByNameArabicAndFaculty(String nameArabic, Faculty faculty);

    List<Department> findByFacultyInstitutionId(Long institutionId);
}
package com.studentbag.backend.grades.repository;

import com.studentbag.backend.grades.entity.GradeCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeCalculationRepository extends JpaRepository<GradeCalculation, Long> {

    List<GradeCalculation> findAllByStudentIdOrderByUpdatedAtDesc(Long studentId);

    Optional<GradeCalculation> findByIdAndStudentId(Long id, Long studentId);
}
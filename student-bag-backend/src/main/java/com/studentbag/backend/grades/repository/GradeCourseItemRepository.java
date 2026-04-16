package com.studentbag.backend.grades.repository;

import com.studentbag.backend.grades.entity.GradeCourseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeCourseItemRepository extends JpaRepository<GradeCourseItem, Long> {

    List<GradeCourseItem> findAllByCalculationIdOrderByOrderIndexAscIdAsc(Long calculationId);

    Optional<GradeCourseItem> findByIdAndCalculationId(Long itemId, Long calculationId);
}
package com.studentbag.backend.resources.repository;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.resources.entity.LearningObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningObjectRepository extends JpaRepository<LearningObject, Long> {

    List<LearningObject> findByCourseIdAndIsActiveTrue(Long courseId);

    List<LearningObject> findByDepartmentNameIgnoreCaseAndIsActiveTrue(String departmentName);

    List<LearningObject> findByCourseIdAndCategoryAndIsActiveTrue(
            Long courseId,
         ResourceCategory category
    );
}
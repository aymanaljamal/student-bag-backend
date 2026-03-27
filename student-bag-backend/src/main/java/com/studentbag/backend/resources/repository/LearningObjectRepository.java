package com.studentbag.backend.resources.repository;

import com.studentbag.backend.domain.enums.ContentFormat;
import com.studentbag.backend.domain.enums.ResourceType;
import com.studentbag.backend.resources.entity.LearningObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningObjectRepository extends JpaRepository<LearningObject, Long> {

    List<LearningObject> findByIsActiveTrue();

    List<LearningObject> findByResourceTypeAndIsActiveTrue(ResourceType resourceType);

    List<LearningObject> findByFormatAndIsActiveTrue(ContentFormat format);

    List<LearningObject> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title);
}
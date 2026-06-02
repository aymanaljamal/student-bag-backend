package com.studentbag.backend.resources.repository;

import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.resources.entity.AdminResource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminResourceRepository extends JpaRepository<AdminResource, Long> {

    List<AdminResource> findByApprovalStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findByCourseIdAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            Long courseId,
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findByCourseIdAndCategoryAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            Long courseId,
            ResourceCategory category,
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findByCourseIdAndResourceTypeAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            Long courseId,
            ResourceType resourceType,
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findByUploadedByTypeAndUploadedByIdAndIsDeletedFalseOrderByCreatedAtDesc(
            ResourceOwnerType uploadedByType,
            Long uploadedById
    );

    List<AdminResource> findByUploadedByTypeAndUploadedByIdAndApprovalStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            ResourceOwnerType uploadedByType,
            Long uploadedById,
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findByLearningObjectIdAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            Long learningObjectId,
            ResourceApprovalStatus approvalStatus
    );

    List<AdminResource> findTop30ByApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            ResourceApprovalStatus approvalStatus
    );
}
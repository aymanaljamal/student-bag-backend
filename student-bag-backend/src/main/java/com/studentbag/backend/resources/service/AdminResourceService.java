package com.studentbag.backend.resources.service;

import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceOwnerType;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.resources.dto.request.ApproveAdminResourceRequest;
import com.studentbag.backend.resources.dto.request.CreateAdminResourceRequest;
import com.studentbag.backend.resources.dto.request.RejectAdminResourceRequest;
import com.studentbag.backend.resources.dto.request.UpdateAdminResourceRequest;
import com.studentbag.backend.resources.dto.response.AdminResourceResponse;
import com.studentbag.backend.resources.dto.response.ResourceApprovalActionResponse;
import com.studentbag.backend.resources.dto.response.ResourceOperationResponse;

import java.util.List;
import java.util.UUID;

public interface AdminResourceService {

    AdminResourceResponse createResource(
            UUID currentUserId,
            ResourceOwnerType uploadedByType,
            CreateAdminResourceRequest request
    );

    List<AdminResourceResponse> getMyUploadedResources(
            UUID currentUserId,
            ResourceOwnerType ownerType
    );

    AdminResourceResponse updateResource(
            Long resourceId,
            UUID currentUserId,
            UpdateAdminResourceRequest request
    );

    AdminResourceResponse getResourceById(Long resourceId);

    List<AdminResourceResponse> getApprovedResourcesByCourse(Long courseId);

    List<AdminResourceResponse> getApprovedResourcesByCourseAndCategory(
            Long courseId,
            ResourceCategory category
    );

    List<AdminResourceResponse> getApprovedResourcesByCourseAndType(
            Long courseId,
            ResourceType resourceType
    );

    List<AdminResourceResponse> getPendingResources();

    List<AdminResourceResponse> getResourcesByApprovalStatus(
            ResourceApprovalStatus status
    );

    List<AdminResourceResponse> getResourcesUploadedBy(
            ResourceOwnerType ownerType,
            Long ownerId
    );

    List<AdminResourceResponse> getResourcesUploadedByAndStatus(
            ResourceOwnerType ownerType,
            Long ownerId,
            ResourceApprovalStatus status
    );

    AdminResourceResponse approveResource(
            Long resourceId,
            UUID adminUserId,
            ApproveAdminResourceRequest request
    );

    AdminResourceResponse rejectResource(
            Long resourceId,
            UUID adminUserId,
            RejectAdminResourceRequest request
    );

    ResourceOperationResponse removeResource(
            Long resourceId,
            UUID adminUserId,
            String adminNote
    );

    ResourceOperationResponse softDeleteResource(
            Long resourceId,
            UUID currentUserId
    );

    List<ResourceApprovalActionResponse> getResourceApprovalHistory(
            Long resourceId
    );
}
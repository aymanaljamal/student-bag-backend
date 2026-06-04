package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.domain.enums.resources.ResourceActionType;
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
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.LearningObject;
import com.studentbag.backend.resources.entity.ResourceApprovalAction;
import com.studentbag.backend.resources.mapper.AdminResourceMapper;
import com.studentbag.backend.resources.mapper.ResourceApprovalActionMapper;
import com.studentbag.backend.resources.repository.AdminResourceRepository;
import com.studentbag.backend.resources.repository.LearningObjectRepository;
import com.studentbag.backend.resources.repository.ResourceApprovalActionRepository;
import com.studentbag.backend.resources.service.AdminResourceService;
import com.studentbag.backend.resources.service.ResourceNotificationService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminResourceServiceImpl implements AdminResourceService {

    private final AdminResourceRepository adminResourceRepository;
    private final LearningObjectRepository learningObjectRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ResourceApprovalActionRepository resourceApprovalActionRepository;
    private final ResourceNotificationService resourceNotificationService;

    @Override
    public AdminResourceResponse createResource(
            UUID currentUserId,
            ResourceOwnerType uploadedByType,
            CreateAdminResourceRequest request
    ) {
        User currentUser = getUser(currentUserId);
        validateUploaderRole(currentUser, uploadedByType);

        Course course = getCourse(request.getCourseId());
        LearningObject learningObject = resolveLearningObject(request.getLearningObjectId());

        AdminResource resource = AdminResourceMapper.toEntity(request);
        resource.setCourse(course);
        resource.setLearningObject(learningObject);
        resource.setUploadedByType(uploadedByType);

        /*
         * uploadedById is still kept for your current business logic.
         * uploadedByUserId is the real UUID used for notifications.
         */
        resource.setUploadedById(resolveBusinessOwnerId(currentUser, uploadedByType));
        resource.setUploadedByUserId(currentUser.getId());

        resource.setApprovalStatus(ResourceApprovalStatus.PENDING);
        resource.setIsVisible(false);
        resource.setIsDeleted(false);

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.SUBMITTED,
                uploadedByType,
                saved.getUploadedById(),
                "Resource submitted for review"
        );

        return AdminResourceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getMyUploadedResources(
            UUID currentUserId,
            ResourceOwnerType ownerType
    ) {
        User currentUser = getUser(currentUserId);
        validateUploaderRole(currentUser, ownerType);

        Long ownerId = resolveBusinessOwnerId(currentUser, ownerType);

        return adminResourceRepository
                .findByUploadedByTypeAndUploadedByIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        ownerType,
                        ownerId
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    public AdminResourceResponse updateResource(
            Long resourceId,
            UUID currentUserId,
            UpdateAdminResourceRequest request
    ) {
        User currentUser = getUser(currentUserId);
        AdminResource resource = getResourceEntity(resourceId);

        validateCanModifyResource(currentUser, resource);

        AdminResourceMapper.updateEntity(resource, request);

        if (request.getCourseId() != null) {
            resource.setCourse(getCourse(request.getCourseId()));
        }

        if (request.getLearningObjectId() != null) {
            resource.setLearningObject(resolveLearningObject(request.getLearningObjectId()));
        }

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.UPDATED,
                resource.getUploadedByType(),
                resource.getUploadedById(),
                "Resource updated"
        );

        return AdminResourceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResourceResponse getResourceById(Long resourceId) {
        return AdminResourceMapper.toResponse(getResourceEntity(resourceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getApprovedResourcesByCourse(Long courseId) {
        return adminResourceRepository
                .findByCourseIdAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                        courseId,
                        ResourceApprovalStatus.APPROVED
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getApprovedResourcesByCourseAndCategory(
            Long courseId,
            ResourceCategory category
    ) {
        return adminResourceRepository
                .findByCourseIdAndCategoryAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                        courseId,
                        category,
                        ResourceApprovalStatus.APPROVED
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getApprovedResourcesByCourseAndType(
            Long courseId,
            ResourceType resourceType
    ) {
        return adminResourceRepository
                .findByCourseIdAndResourceTypeAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                        courseId,
                        resourceType,
                        ResourceApprovalStatus.APPROVED
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getPendingResources() {
        return adminResourceRepository
                .findByApprovalStatusAndIsDeletedFalseOrderByCreatedAtDesc(ResourceApprovalStatus.PENDING)
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getResourcesByApprovalStatus(ResourceApprovalStatus status) {
        return adminResourceRepository
                .findByApprovalStatusAndIsDeletedFalseOrderByCreatedAtDesc(status)
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getResourcesUploadedBy(
            ResourceOwnerType ownerType,
            Long ownerId
    ) {
        return adminResourceRepository
                .findByUploadedByTypeAndUploadedByIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        ownerType,
                        ownerId
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResourceResponse> getResourcesUploadedByAndStatus(
            ResourceOwnerType ownerType,
            Long ownerId,
            ResourceApprovalStatus status
    ) {
        return adminResourceRepository
                .findByUploadedByTypeAndUploadedByIdAndApprovalStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                        ownerType,
                        ownerId,
                        status
                )
                .stream()
                .map(AdminResourceMapper::toResponse)
                .toList();
    }

    @Override
    public AdminResourceResponse approveResource(
            Long resourceId,
            UUID adminUserId,
            ApproveAdminResourceRequest request
    ) {
        User admin = getAdminUser(adminUserId);
        AdminResource resource = getResourceEntity(resourceId);

        ensureUploadedByUserId(resource);

        resource.setApprovalStatus(ResourceApprovalStatus.APPROVED);
        resource.setIsVisible(true);
        resource.setApprovedByAdminId(resolveBusinessOwnerId(admin, ResourceOwnerType.ADMIN));
        resource.setAdminNotes(request != null ? request.getAdminNotes() : null);

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.APPROVED,
                ResourceOwnerType.ADMIN,
                saved.getApprovedByAdminId(),
                request != null ? request.getAdminNotes() : "Approved"
        );

        resourceNotificationService.notifyInstructorResourceApproved(saved, adminUserId);

        return AdminResourceMapper.toResponse(saved);
    }

    @Override
    public AdminResourceResponse rejectResource(
            Long resourceId,
            UUID adminUserId,
            RejectAdminResourceRequest request
    ) {
        User admin = getAdminUser(adminUserId);
        AdminResource resource = getResourceEntity(resourceId);

        ensureUploadedByUserId(resource);

        resource.setApprovalStatus(ResourceApprovalStatus.REJECTED);
        resource.setIsVisible(false);
        resource.setApprovedByAdminId(resolveBusinessOwnerId(admin, ResourceOwnerType.ADMIN));
        resource.setAdminNotes(request.getAdminNotes());

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.REJECTED,
                ResourceOwnerType.ADMIN,
                saved.getApprovedByAdminId(),
                request.getAdminNotes()
        );

        resourceNotificationService.notifyInstructorResourceRejected(
                saved,
                adminUserId,
                request.getAdminNotes()
        );

        return AdminResourceMapper.toResponse(saved);
    }

    @Override
    public ResourceOperationResponse removeResource(
            Long resourceId,
            UUID adminUserId,
            String adminNote
    ) {
        User admin = getAdminUser(adminUserId);
        AdminResource resource = getResourceEntity(resourceId);

        ensureUploadedByUserId(resource);

        resource.setApprovalStatus(ResourceApprovalStatus.REMOVED);
        resource.setIsVisible(false);
        resource.setApprovedByAdminId(resolveBusinessOwnerId(admin, ResourceOwnerType.ADMIN));
        resource.setAdminNotes(adminNote);

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.DELETED,
                ResourceOwnerType.ADMIN,
                saved.getApprovedByAdminId(),
                adminNote
        );

        resourceNotificationService.notifyInstructorResourceRemoved(
                saved,
                adminUserId,
                adminNote
        );

        return ResourceOperationResponse.builder()
                .targetId(resourceId)
                .operation("REMOVE_RESOURCE")
                .success(true)
                .message("Resource removed successfully")
                .build();
    }

    @Override
    public ResourceOperationResponse softDeleteResource(
            Long resourceId,
            UUID currentUserId
    ) {
        User currentUser = getUser(currentUserId);
        AdminResource resource = getResourceEntity(resourceId);

        validateCanModifyResource(currentUser, resource);

        ensureUploadedByUserId(resource);

        resource.setIsDeleted(true);
        resource.setIsVisible(false);

        AdminResource saved = adminResourceRepository.save(resource);

        saveApprovalAction(
                saved,
                ResourceActionType.DELETED,
                currentUser.getRole() == UserRole.ADMINISTRATOR
                        ? ResourceOwnerType.ADMIN
                        : saved.getUploadedByType(),
                currentUser.getRole() == UserRole.ADMINISTRATOR
                        ? resolveBusinessOwnerId(currentUser, ResourceOwnerType.ADMIN)
                        : saved.getUploadedById(),
                "Resource deleted"
        );

        boolean deletedByAdmin = currentUser.getRole() == UserRole.ADMINISTRATOR;
        boolean ownerIsDifferentUser = saved.getUploadedByUserId() != null
                && !saved.getUploadedByUserId().equals(currentUser.getId());

        if (deletedByAdmin && ownerIsDifferentUser) {
            resourceNotificationService.notifyInstructorResourceRemoved(
                    saved,
                    currentUserId,
                    "Resource deleted by admin"
            );
        }

        return ResourceOperationResponse.builder()
                .targetId(resourceId)
                .operation("SOFT_DELETE_RESOURCE")
                .success(true)
                .message("Resource deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceApprovalActionResponse> getResourceApprovalHistory(Long resourceId) {
        getResourceEntity(resourceId);

        return resourceApprovalActionRepository
                .findByAdminResourceIdOrderByCreatedAtDesc(resourceId)
                .stream()
                .map(ResourceApprovalActionMapper::toResponse)
                .toList();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private User getAdminUser(UUID adminUserId) {
        User admin = getUser(adminUserId);

        if (admin.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Only administrator can perform this action");
        }

        return admin;
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    private LearningObject resolveLearningObject(Long learningObjectId) {
        if (learningObjectId == null) {
            return null;
        }

        return learningObjectRepository.findById(learningObjectId)
                .orElseThrow(() -> new IllegalArgumentException("Learning object not found"));
    }

    private AdminResource getResourceEntity(Long resourceId) {
        return adminResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Admin resource not found"));
    }

    private void validateUploaderRole(User currentUser, ResourceOwnerType uploadedByType) {
        if (uploadedByType == ResourceOwnerType.ADMIN &&
                currentUser.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Only admin can upload as ADMIN");
        }

        if (uploadedByType == ResourceOwnerType.INSTRUCTOR &&
                currentUser.getRole() != UserRole.INSTRUCTOR) {
            throw new IllegalArgumentException("Only instructor can upload as INSTRUCTOR");
        }

        if (uploadedByType == ResourceOwnerType.STUDENT &&
                currentUser.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only student can upload as STUDENT");
        }
    }

    private void validateCanModifyResource(User currentUser, AdminResource resource) {
        if (currentUser.getRole() == UserRole.ADMINISTRATOR) {
            return;
        }

        if (resource.getUploadedByUserId() != null &&
                resource.getUploadedByUserId().equals(currentUser.getId())) {
            return;
        }

        Long currentBusinessId = resolveBusinessOwnerId(
                currentUser,
                resource.getUploadedByType()
        );

        if (!resource.getUploadedById().equals(currentBusinessId)) {
            throw new IllegalArgumentException("Unauthorized to modify this resource");
        }
    }

    /*
     * Keep this only for compatibility with your current uploadedById field.
     * Do not use uploadedById for notifications.
     */
    private Long resolveBusinessOwnerId(User user, ResourceOwnerType ownerType) {
        return Math.abs(user.getId().hashCode()) + 0L;
    }

    /*
     * This protects old resources that were created before adding uploadedByUserId.
     * New resources will always have uploadedByUserId from createResource().
     */
    private void ensureUploadedByUserId(AdminResource resource) {
        if (resource.getUploadedByUserId() != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Resource owner user id is missing. This resource was probably created before uploadedByUserId was added."
        );
    }

    private void saveApprovalAction(
            AdminResource resource,
            ResourceActionType actionType,
            ResourceOwnerType actorType,
            Long actorId,
            String note
    ) {
        ResourceApprovalAction action = ResourceApprovalAction.builder()
                .adminResource(resource)
                .actionType(actionType)
                .actorType(actorType)
                .actorId(actorId)
                .note(note)
                .build();

        resourceApprovalActionRepository.save(action);
    }
}
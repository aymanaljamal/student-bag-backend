package com.studentbag.backend.resources.controller;

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
import com.studentbag.backend.resources.service.AdminResourceService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing public/general resources.
 *
 * <p>This controller handles:
 * <ul>
 *     <li>Submitting public resources by instructors/admins/students</li>
 *     <li>Updating submitted resources</li>
 *     <li>Approving/rejecting/removing resources by admin</li>
 *     <li>Fetching approved resources for public library browsing</li>
 *     <li>Fetching workflow history for moderation actions</li>
 * </ul>
 *
 * <p>Authentication:
 * current user is resolved from JWT through {@link UserDetails},
 * then mapped to the internal user UUID using email.</p>
 */
@RestController
@RequestMapping("/api/resources/admin")
@RequiredArgsConstructor
public class AdminResourceController {

    private final AdminResourceService adminResourceService;
    private final UserRepository userRepository;

    /**
     * Resolves the current authenticated user UUID from JWT email.
     */
    private UUID getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return user.getId();
    }
    @GetMapping("/me")
    public ResponseEntity<List<AdminResourceResponse>> getMyUploadedResources(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam ResourceOwnerType ownerType
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.getMyUploadedResources(currentUserId, ownerType)
        );
    }
    /**
     * Submits a new public resource.
     *
     * <p>This endpoint is typically used by instructor/admin,
     * and the resource enters moderation workflow as PENDING.</p>
     */
    @PostMapping
    public ResponseEntity<AdminResourceResponse> createResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam ResourceOwnerType uploadedByType,
            @Valid @RequestBody CreateAdminResourceRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.createResource(currentUserId, uploadedByType, request)
        );
    }

    /**
     * Updates an existing public resource.
     *
     * <p>Allowed for uploader or admin according to service authorization rules.</p>
     */
    @PutMapping("/{resourceId}")
    public ResponseEntity<AdminResourceResponse> updateResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateAdminResourceRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.updateResource(resourceId, currentUserId, request)
        );
    }

    /**
     * Returns a single public resource by id.
     */
    @GetMapping("/{resourceId}")
    public ResponseEntity<AdminResourceResponse> getResourceById(
            @PathVariable Long resourceId
    ) {
        return ResponseEntity.ok(adminResourceService.getResourceById(resourceId));
    }

    /**
     * Returns approved and visible public resources by course.
     */
    @GetMapping("/approved/course/{courseId}")
    public ResponseEntity<List<AdminResourceResponse>> getApprovedResourcesByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                adminResourceService.getApprovedResourcesByCourse(courseId)
        );
    }

    /**
     * Returns approved and visible public resources by course and category.
     */
    @GetMapping("/approved/course/{courseId}/category/{category}")
    public ResponseEntity<List<AdminResourceResponse>> getApprovedResourcesByCourseAndCategory(
            @PathVariable Long courseId,
            @PathVariable ResourceCategory category
    ) {
        return ResponseEntity.ok(
                adminResourceService.getApprovedResourcesByCourseAndCategory(courseId, category)
        );
    }

    /**
     * Returns approved and visible public resources by course and resource type.
     */
    @GetMapping("/approved/course/{courseId}/type/{resourceType}")
    public ResponseEntity<List<AdminResourceResponse>> getApprovedResourcesByCourseAndType(
            @PathVariable Long courseId,
            @PathVariable ResourceType resourceType
    ) {
        return ResponseEntity.ok(
                adminResourceService.getApprovedResourcesByCourseAndType(courseId, resourceType)
        );
    }

    /**
     * Returns all pending resources awaiting moderation.
     *
     * <p>Admin use case.</p>
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AdminResourceResponse>> getPendingResources() {
        return ResponseEntity.ok(adminResourceService.getPendingResources());
    }

    /**
     * Returns resources filtered by workflow status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminResourceResponse>> getResourcesByStatus(
            @PathVariable ResourceApprovalStatus status
    ) {
        return ResponseEntity.ok(
                adminResourceService.getResourcesByApprovalStatus(status)
        );
    }

    /**
     * Returns all resources uploaded by a specific owner.
     */
    @GetMapping("/uploaded-by")
    public ResponseEntity<List<AdminResourceResponse>> getResourcesUploadedBy(
            @RequestParam ResourceOwnerType ownerType,
            @RequestParam Long ownerId
    ) {
        return ResponseEntity.ok(
                adminResourceService.getResourcesUploadedBy(ownerType, ownerId)
        );
    }

    /**
     * Returns resources uploaded by a specific owner filtered by status.
     */
    @GetMapping("/uploaded-by/status")
    public ResponseEntity<List<AdminResourceResponse>> getResourcesUploadedByAndStatus(
            @RequestParam ResourceOwnerType ownerType,
            @RequestParam Long ownerId,
            @RequestParam ResourceApprovalStatus status
    ) {
        return ResponseEntity.ok(
                adminResourceService.getResourcesUploadedByAndStatus(ownerType, ownerId, status)
        );
    }

    /**
     * Approves a submitted resource.
     *
     * <p>Admin action. Also triggers workflow notification to the uploader.</p>
     */
    @PostMapping("/{resourceId}/approve")
    public ResponseEntity<AdminResourceResponse> approveResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) ApproveAdminResourceRequest request
    ) {
        UUID adminUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.approveResource(resourceId, adminUserId, request)
        );
    }

    /**
     * Rejects a submitted resource.
     *
     * <p>Admin action. Also triggers workflow notification to the uploader.</p>
     */
    @PostMapping("/{resourceId}/reject")
    public ResponseEntity<AdminResourceResponse> rejectResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RejectAdminResourceRequest request
    ) {
        UUID adminUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.rejectResource(resourceId, adminUserId, request)
        );
    }

    /**
     * Removes an already submitted/approved resource from the public library.
     *
     * <p>Admin action. Keeps workflow history and sends notification.</p>
     */
    @PostMapping("/{resourceId}/remove")
    public ResponseEntity<ResourceOperationResponse> removeResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String adminNote
    ) {
        UUID adminUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.removeResource(resourceId, adminUserId, adminNote)
        );
    }

    /**
     * Soft deletes a public resource.
     *
     * <p>Used by uploader or admin depending on business rules.</p>
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<ResourceOperationResponse> softDeleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                adminResourceService.softDeleteResource(resourceId, currentUserId)
        );
    }

    /**
     * Returns moderation history for the resource.
     */
    @GetMapping("/{resourceId}/history")
    public ResponseEntity<List<ResourceApprovalActionResponse>> getResourceApprovalHistory(
            @PathVariable Long resourceId
    ) {
        return ResponseEntity.ok(
                adminResourceService.getResourceApprovalHistory(resourceId)
        );
    }
}
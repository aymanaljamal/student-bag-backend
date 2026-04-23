package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.notifications.repository.UserDeviceTokenRepository;
import com.studentbag.backend.notifications.service.FirebasePushNotificationService;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.service.ResourceNotificationService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ResourceNotificationService}.
 *
 * <p>This service sends Firebase push notifications related to
 * public resource moderation workflow:
 * <ul>
 *     <li>Approved resource</li>
 *     <li>Rejected resource</li>
 *     <li>Removed resource</li>
 * </ul>
 *
 * <p>Important:
 * If uploadedById in {@link AdminResource} is not the same as application user id,
 * replace the resolution logic with your own instructor/admin/student lookup.</p>
 */
@Service
@RequiredArgsConstructor
public class ResourceNotificationServiceImpl implements ResourceNotificationService {

    private final UserRepository userRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final FirebasePushNotificationService firebasePushNotificationService;

    @Override
    public void notifyInstructorResourceApproved(AdminResource resource, UUID adminUserId) {
        User targetUser = resolveUploaderUser(resource);
        if (targetUser == null) {
            return;
        }

        List<String> tokens = userDeviceTokenRepository.findActiveTokensByUserId(targetUser.getId());

        firebasePushNotificationService.sendToTokens(
                tokens,
                "Resource approved",
                "Your resource \"" + resource.getTitle() + "\" has been approved.",
                "/instructor/resources",
                "RESOURCE_APPROVED"
        );
    }

    @Override
    public void notifyInstructorResourceRejected(AdminResource resource, UUID adminUserId, String reason) {
        User targetUser = resolveUploaderUser(resource);
        if (targetUser == null) {
            return;
        }

        List<String> tokens = userDeviceTokenRepository.findActiveTokensByUserId(targetUser.getId());

        firebasePushNotificationService.sendToTokens(
                tokens,
                "Resource rejected",
                reason == null || reason.isBlank()
                        ? "Your resource \"" + resource.getTitle() + "\" was rejected."
                        : "Your resource \"" + resource.getTitle() + "\" was rejected. Reason: " + reason,
                "/instructor/resources",
                "RESOURCE_REJECTED"
        );
    }

    @Override
    public void notifyInstructorResourceRemoved(AdminResource resource, UUID adminUserId, String reason) {
        User targetUser = resolveUploaderUser(resource);
        if (targetUser == null) {
            return;
        }

        List<String> tokens = userDeviceTokenRepository.findActiveTokensByUserId(targetUser.getId());

        firebasePushNotificationService.sendToTokens(
                tokens,
                "Resource removed",
                reason == null || reason.isBlank()
                        ? "Your resource \"" + resource.getTitle() + "\" was removed by admin."
                        : "Your resource \"" + resource.getTitle() + "\" was removed by admin. Reason: " + reason,
                "/instructor/resources",
                "RESOURCE_REMOVED"
        );
    }

    /**
     * Resolves uploader user from resource uploader id.
     *
     * <p>Current implementation assumes uploadedById can be mapped directly
     * to your application user table through a custom convention.
     * Replace this logic with real instructor/student/admin entity lookup if needed.</p>
     */
    private User resolveUploaderUser(AdminResource resource) {
        // TODO:
        // Replace with actual mapping logic if uploadedById represents Instructor.id / Student.id / Admin.id
        return userRepository.findAll().stream()
                .filter(user -> Math.abs(user.getId().hashCode()) + 0L == resource.getUploadedById())
                .findFirst()
                .orElse(null);
    }
}
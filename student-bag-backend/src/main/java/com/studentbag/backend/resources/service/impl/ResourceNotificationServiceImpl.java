package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.service.ResourceNotificationService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceNotificationServiceImpl implements ResourceNotificationService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public void notifyInstructorResourceApproved(AdminResource resource, UUID adminUserId) {
        User targetUser = resolveUploaderUser(resource);

        if (targetUser == null) {
            return;
        }

        sendResourceNotification(
                targetUser,
                "Resource approved",
                buildApprovedBody(resource),
                "/instructor/resources",
                NotificationType.SYSTEM
        );
    }

    @Override
    public void notifyInstructorResourceRejected(
            AdminResource resource,
            UUID adminUserId,
            String reason
    ) {
        User targetUser = resolveUploaderUser(resource);

        if (targetUser == null) {
            return;
        }

        sendResourceNotification(
                targetUser,
                "Resource rejected",
                buildRejectedBody(resource, reason),
                "/instructor/resources",
                NotificationType.SYSTEM
        );
    }

    @Override
    public void notifyInstructorResourceRemoved(
            AdminResource resource,
            UUID adminUserId,
            String reason
    ) {
        User targetUser = resolveUploaderUser(resource);

        if (targetUser == null) {
            return;
        }

        sendResourceNotification(
                targetUser,
                "Resource deleted",
                buildRemovedBody(resource, reason),
                "/instructor/resources",
                NotificationType.SYSTEM
        );
    }

    private void sendResourceNotification(
            User targetUser,
            String title,
            String body,
            String targetValue,
            NotificationType type
    ) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(title);
        request.setBody(body);
        request.setType(type);
        request.setPriority(NotificationPriority.NORMAL);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue(targetValue);
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(List.of(targetUser.getId()));

        notificationService.createAndSend(request);
    }

    private User resolveUploaderUser(AdminResource resource) {
        if (resource == null || resource.getUploadedByUserId() == null) {
            return null;
        }

        return userRepository.findById(resource.getUploadedByUserId())
                .orElse(null);
    }

    private String buildApprovedBody(AdminResource resource) {
        return "Your resource \"" + safeTitle(resource) + "\" has been approved.";
    }

    private String buildRejectedBody(AdminResource resource, String reason) {
        if (reason == null || reason.isBlank()) {
            return "Your resource \"" + safeTitle(resource) + "\" was rejected.";
        }

        return "Your resource \"" + safeTitle(resource) + "\" was rejected. Reason: " + reason.trim();
    }

    private String buildRemovedBody(AdminResource resource, String reason) {
        if (reason == null || reason.isBlank()) {
            return "Your resource \"" + safeTitle(resource) + "\" was deleted by admin.";
        }

        return "Your resource \"" + safeTitle(resource) + "\" was deleted by admin. Reason: " + reason.trim();
    }

    private String safeTitle(AdminResource resource) {
        if (resource == null || resource.getTitle() == null || resource.getTitle().isBlank()) {
            return "Untitled resource";
        }

        return resource.getTitle().trim();
    }
}
package com.studentbag.backend.notifications.controller;

import com.studentbag.backend.notifications.dto.request.CreateAdminNotificationRequest;
import com.studentbag.backend.notifications.dto.request.SaveDeviceTokenRequest;
import com.studentbag.backend.notifications.dto.response.DeleteNotificationsResponse;
import com.studentbag.backend.notifications.dto.response.NotificationResponse;
import com.studentbag.backend.notifications.dto.response.UnreadCountResponse;
import com.studentbag.backend.notifications.service.DeviceTokenService;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return user.getId();
    }
    @DeleteMapping("/me/{userNotificationId}")
    public ResponseEntity<DeleteNotificationsResponse> deleteMyNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userNotificationId
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                notificationService.deleteMyNotification(userId, userNotificationId)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<DeleteNotificationsResponse> deleteAllMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                notificationService.deleteAllMyNotifications(userId)
        );
    }
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/admin/send")
    public ResponseEntity<NotificationResponse> sendAdminNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAdminNotificationRequest request
    ) {
        UUID adminUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                notificationService.createAndSendAdminNotification(adminUserId, request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, page, size));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                new UnreadCountResponse(notificationService.getUnreadCount(userId))
        );
    }

    @PutMapping("/me/{userNotificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userNotificationId
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                notificationService.markAsRead(userId, userNotificationId)
        );
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<String> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = getCurrentUserId(userDetails);
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("Marked " + updated + " notifications as read");
    }

    @PostMapping("/devices/token")
    public ResponseEntity<String> saveDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveDeviceTokenRequest request
    ) {
        UUID userId = getCurrentUserId(userDetails);
        deviceTokenService.saveToken(userId, request);
        return ResponseEntity.ok("Token saved successfully");
    }
}
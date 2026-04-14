package com.studentbag.backend.notifications.controller;

import com.studentbag.backend.notifications.dto.request.CreateAdminNotificationRequest;
import com.studentbag.backend.notifications.dto.request.SaveDeviceTokenRequest;
import com.studentbag.backend.notifications.dto.response.NotificationResponse;
import com.studentbag.backend.notifications.dto.response.UnreadCountResponse;
import com.studentbag.backend.notifications.service.DeviceTokenService;
import com.studentbag.backend.notifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/send")
    public ResponseEntity<NotificationResponse> sendAdminNotification(
            @AuthenticationPrincipal(expression = "id") UUID adminUserId,
            @Valid @RequestBody CreateAdminNotificationRequest request
    ) {
        return ResponseEntity.ok(
                notificationService.createAndSendAdminNotification(adminUserId, request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, page, size));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        return ResponseEntity.ok(
                new UnreadCountResponse(notificationService.getUnreadCount(userId))
        );
    }

    @PutMapping("/me/{userNotificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @PathVariable UUID userNotificationId
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(userId, userNotificationId)
        );
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<String> markAllAsRead(
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("Marked " + updated + " notifications as read");
    }

    @PostMapping("/devices/token")
    public ResponseEntity<String> saveDeviceToken(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody SaveDeviceTokenRequest request
    ) {
        deviceTokenService.saveToken(userId, request);
        return ResponseEntity.ok("Token saved successfully");
    }
}
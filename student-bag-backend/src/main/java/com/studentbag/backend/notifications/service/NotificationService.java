package com.studentbag.backend.notifications.service;

import com.studentbag.backend.notifications.dto.request.CreateAdminNotificationRequest;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.dto.response.DeleteNotificationsResponse;
import com.studentbag.backend.notifications.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createAndSend(CreateNotificationRequest request);
    DeleteNotificationsResponse deleteMyNotification(UUID userId, UUID userNotificationId);

    DeleteNotificationsResponse deleteAllMyNotifications(UUID userId);
    NotificationResponse createAndSendAdminNotification(UUID adminUserId, CreateAdminNotificationRequest request);

    List<NotificationResponse> getMyNotifications(UUID userId, int page, int size);

    long getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID userId, UUID userNotificationId);

    int markAllAsRead(UUID userId);

    void dispatchTaskReminderNotifications();

    void dispatchRecurringTaskNotifications();

    void dispatchEventNotifications();
}
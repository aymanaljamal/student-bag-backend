package com.studentbag.backend.notifications.service;

import com.studentbag.backend.notifications.dto.request.UpdateStudentNotificationPreferenceRequest;
import com.studentbag.backend.notifications.dto.response.StudentNotificationPreferenceResponse;

import java.util.UUID;

public interface StudentNotificationPreferenceService {
    StudentNotificationPreferenceResponse getMyPreferences(UUID userId);
    StudentNotificationPreferenceResponse updateMyPreferences(
            UUID userId,
            UpdateStudentNotificationPreferenceRequest request
    );
}
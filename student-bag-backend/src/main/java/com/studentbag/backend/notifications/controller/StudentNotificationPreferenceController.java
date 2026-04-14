package com.studentbag.backend.notifications.controller;

import com.studentbag.backend.notifications.dto.request.UpdateStudentNotificationPreferenceRequest;
import com.studentbag.backend.notifications.dto.response.StudentNotificationPreferenceResponse;
import com.studentbag.backend.notifications.service.StudentNotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/student/notification-preferences")
@RequiredArgsConstructor
public class StudentNotificationPreferenceController {

    private final StudentNotificationPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<StudentNotificationPreferenceResponse> getMyPreferences(
            @AuthenticationPrincipal(expression = "id") UUID userId
    ) {
        return ResponseEntity.ok(preferenceService.getMyPreferences(userId));
    }

    @PutMapping
    public ResponseEntity<StudentNotificationPreferenceResponse> updateMyPreferences(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody UpdateStudentNotificationPreferenceRequest request
    ) {
        return ResponseEntity.ok(preferenceService.updateMyPreferences(userId, request));
    }
}
package com.studentbag.backend.notifications.controller;

import com.studentbag.backend.notifications.dto.request.UpdateStudentNotificationPreferenceRequest;
import com.studentbag.backend.notifications.dto.response.StudentNotificationPreferenceResponse;
import com.studentbag.backend.notifications.service.StudentNotificationPreferenceService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/student/notification-preferences")
@RequiredArgsConstructor
public class StudentNotificationPreferenceController {

    private final StudentNotificationPreferenceService preferenceService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return user.getId();
    }

    @GetMapping
    public ResponseEntity<StudentNotificationPreferenceResponse> getMyPreferences(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(preferenceService.getMyPreferences(userId));
    }

    @PutMapping
    public ResponseEntity<StudentNotificationPreferenceResponse> updateMyPreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateStudentNotificationPreferenceRequest request
    ) {
        UUID userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(preferenceService.updateMyPreferences(userId, request));
    }
}
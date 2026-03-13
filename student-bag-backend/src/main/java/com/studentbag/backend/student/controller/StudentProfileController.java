package com.studentbag.backend.student.controller;

import com.studentbag.backend.student.dto.request.UpdateStudentProfileRequest;
import com.studentbag.backend.student.dto.response.StudentProfileResponse;
import com.studentbag.backend.student.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    public StudentProfileResponse getMyProfile(@AuthenticationPrincipal(expression = "id") UUID userId) {
        return studentProfileService.getMyProfile(userId);
    }

    @PutMapping("/me")
    public StudentProfileResponse updateMyProfile(
            @AuthenticationPrincipal(expression = "id") UUID userId,
            @RequestBody UpdateStudentProfileRequest request
    ) {
        return studentProfileService.updateMyProfile(userId, request);
    }
}
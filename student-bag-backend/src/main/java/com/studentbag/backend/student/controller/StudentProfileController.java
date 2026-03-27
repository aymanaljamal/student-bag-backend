package com.studentbag.backend.student.controller;

import com.studentbag.backend.student.dto.request.UpdateStudentProfileRequest;
import com.studentbag.backend.student.dto.response.StudentProfileResponse;
import com.studentbag.backend.student.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    public StudentProfileResponse getMyProfile(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        return studentProfileService.getMyProfileByEmail(email);
    }

    @PutMapping("/me")
    public StudentProfileResponse updateMyProfile(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestBody UpdateStudentProfileRequest request
    ) {
        return studentProfileService.updateMyProfileByEmail(email, request);
    }
}
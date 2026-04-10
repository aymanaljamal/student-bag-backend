package com.studentbag.backend.instructor.controller;

import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    /**
     * Public profile for students / users
     * Example: GET /api/instructors/5/profile
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<InstructorProfileResponse> getInstructorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getPublicProfile(id));
    }

    /**
     * Logged-in instructor profile
     * Example: GET /api/instructors/me/profile
     */
    @GetMapping("/me/profile")
    public ResponseEntity<InstructorProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                instructorService.getMyProfile(authentication.getName())
        );
    }
}
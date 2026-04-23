package com.studentbag.backend.instructor.controller;

import com.studentbag.backend.instructor.dto.request.InstructorUpdateProfileRequest;
import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping("/{id}/profile")
    public ResponseEntity<InstructorProfileResponse> getInstructorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getPublicProfile(id));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<InstructorProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                instructorService.getMyProfile(authentication.getName())
        );
    }

    @PutMapping("/me/profile")
    public ResponseEntity<InstructorProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody InstructorUpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                instructorService.updateMyProfile(authentication.getName(), request)
        );
    }
}
package com.studentbag.backend.administrator.controller;

import com.studentbag.backend.administrator.dto.request.AdminUpdateManagedUserRequest;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserDetailsResponse;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserSummaryResponse;
import com.studentbag.backend.administrator.service.AdminUserManagementService;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.domain.enums.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/administrator/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping("/by-role/{role}")
    public List<AdminManagedUserSummaryResponse> getUsersByRole(@PathVariable UserRole role) {
        return adminUserManagementService.getUsersByRole(role);
    }

    @GetMapping("/{userId}")
    public AdminManagedUserDetailsResponse getUserDetails(@PathVariable UUID userId) {
        return adminUserManagementService.getUserDetails(userId);
    }

    @PutMapping("/{userId}/basic-info")
    public AdminManagedUserDetailsResponse updateUserBasicInfo(
            @PathVariable UUID userId,
            @RequestBody AdminUpdateManagedUserRequest request
    ) {
        return adminUserManagementService.updateUserBasicInfo(userId, request);
    }

    @PostMapping("/students")
    public AuthResponse createStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return adminUserManagementService.createStudent(request);
    }

    @PostMapping("/instructors")
    public AuthResponse createInstructor(@Valid @RequestBody InstructorRegisterRequest request) {
        return adminUserManagementService.createInstructor(request);
    }
}
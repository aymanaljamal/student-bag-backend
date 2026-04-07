package com.studentbag.backend.administrator.service;

import com.studentbag.backend.administrator.dto.request.AdminUpdateManagedUserRequest;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserDetailsResponse;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserSummaryResponse;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.domain.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface AdminUserManagementService {

    List<AdminManagedUserSummaryResponse> getUsersByRole(UserRole role);

    AdminManagedUserDetailsResponse getUserDetails(UUID userId);

    AdminManagedUserDetailsResponse updateUserBasicInfo(UUID userId, AdminUpdateManagedUserRequest request);

    AuthResponse createStudent(StudentRegisterRequest request);

   // AuthResponse createInstructor(InstructorRegisterRequest request);
}
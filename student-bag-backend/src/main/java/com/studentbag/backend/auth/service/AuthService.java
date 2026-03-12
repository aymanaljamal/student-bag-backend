package com.studentbag.backend.auth.service;

import com.studentbag.backend.auth.dto.request.AdministratorRegisterRequest;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.ParentRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse registerStudent(StudentRegisterRequest request);

    AuthResponse registerParent(ParentRegisterRequest request);

    AuthResponse registerInstructor(InstructorRegisterRequest request);

    AuthResponse registerAdministrator(AdministratorRegisterRequest request);

    AuthResponse login(LoginRequest request);
}
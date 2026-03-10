package com.studentbag.backend.auth.service;

import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.RegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
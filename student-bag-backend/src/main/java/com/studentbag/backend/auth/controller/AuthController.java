package com.studentbag.backend.auth.controller;

import com.studentbag.backend.auth.dto.request.AdministratorRegisterRequest;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.ParentRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public AuthResponse registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/register/parent")
    public AuthResponse registerParent(@Valid @RequestBody ParentRegisterRequest request) {
        return authService.registerParent(request);
    }

    @PostMapping("/register/instructor")
    public AuthResponse registerInstructor(@Valid @RequestBody InstructorRegisterRequest request) {
        return authService.registerInstructor(request);
    }

    @PostMapping("/register/admin")
    public AuthResponse registerAdministrator(@Valid @RequestBody AdministratorRegisterRequest request) {
        return authService.registerAdministrator(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
package com.studentbag.backend.auth.controller;

import com.studentbag.backend.auth.dto.request.ForgotPasswordRequest;
import com.studentbag.backend.auth.dto.request.ResetPasswordRequest;
import com.studentbag.backend.auth.dto.request.VerifyResetCodeRequest;
import com.studentbag.backend.auth.dto.response.ApiMessageResponse;
import com.studentbag.backend.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ApiMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.getEmail());
        return new ApiMessageResponse("Password reset code sent to email");
    }

    @PostMapping("/verify-code")
    public ApiMessageResponse verifyCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        passwordResetService.verifyCode(request.getEmail(), request.getCode());
        return new ApiMessageResponse("Code is valid");
    }

    @PostMapping("/reset")
    public ApiMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        );
        return new ApiMessageResponse("Password reset successfully");
    }
}
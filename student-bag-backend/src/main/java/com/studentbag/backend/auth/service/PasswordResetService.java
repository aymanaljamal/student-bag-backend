package com.studentbag.backend.auth.service;

public interface PasswordResetService {
    void sendResetCode(String email);
    void verifyCode(String email, String code);
    void resetPassword(String email, String code, String newPassword);
}
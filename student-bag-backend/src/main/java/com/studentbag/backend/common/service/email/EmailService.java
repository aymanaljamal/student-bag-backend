package com.studentbag.backend.common.service.email;

public interface EmailService {
    void sendPasswordResetCode(String toEmail, String fullName, String code);
}
package com.studentbag.backend.common.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetCode(String toEmail, String fullName, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Student Bag - Password Reset Code");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Your password reset code is: " + code + "\n\n" +
                        "This code will expire in 10 minutes.\n\n" +
                        "If you did not request this, please ignore this email."
        );
        mailSender.send(message);
    }
}
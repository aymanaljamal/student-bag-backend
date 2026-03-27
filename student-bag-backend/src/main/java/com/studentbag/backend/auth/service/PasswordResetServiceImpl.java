package com.studentbag.backend.auth.service;

import com.studentbag.backend.auth.entity.PasswordResetToken;
import com.studentbag.backend.auth.repository.PasswordResetTokenRepository;
import com.studentbag.backend.auth.service.PasswordResetService;
import com.studentbag.backend.common.service.email.EmailService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tokenRepository.deleteByUser(user);

        String code = generateCode();

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        tokenRepository.save(token);

        emailService.sendPasswordResetCode(user.getEmail(), user.getFullName(), code);
    }

    @Override
    public void verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken token = tokenRepository
                .findTopByUserAndCodeAndUsedFalseOrderByCreatedAtDesc(user, code)
                .orElseThrow(() -> new RuntimeException("Invalid code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken token = tokenRepository
                .findTopByUserAndCodeAndUsedFalseOrderByCreatedAtDesc(user, code)
                .orElseThrow(() -> new RuntimeException("Invalid code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}
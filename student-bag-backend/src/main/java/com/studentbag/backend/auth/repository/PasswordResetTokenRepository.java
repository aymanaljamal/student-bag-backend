package com.studentbag.backend.auth.repository;

import com.studentbag.backend.auth.entity.PasswordResetToken;
import com.studentbag.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findTopByUserAndCodeAndUsedFalseOrderByCreatedAtDesc(User user, String code);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
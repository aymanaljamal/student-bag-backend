package com.studentbag.backend.notifications.service.impl;

import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentNotificationUserResolver {

    private final UserRepository userRepository;

    public User resolve(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required.");
        }

        String principal = authentication.getName();

        try {
            UUID userId = UUID.fromString(principal);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user was not found."));
        } catch (IllegalArgumentException ignored) {
            return userRepository.findByEmail(principal)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user was not found."));
        }
    }
}
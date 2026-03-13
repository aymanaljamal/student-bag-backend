package com.studentbag.backend.auth.dto.response;

import com.studentbag.backend.domain.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UUID userId;
    private String fullName;
    private String email;
    private UserRole role;
    private String avatarUrl;
}
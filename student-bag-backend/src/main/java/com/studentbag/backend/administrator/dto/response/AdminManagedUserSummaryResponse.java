package com.studentbag.backend.administrator.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminManagedUserSummaryResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
}
package com.studentbag.backend.administrator.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminManagedUserDetailsResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String languageCode;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private boolean phoneVerified;

    private Long domainProfileId;

    // student
    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;
    private Boolean gpaVisibleToParents;

    // instructor
    private String department;

    // admin
    private String adminScope;

    private Long institutionId;
    private String institutionName;
}
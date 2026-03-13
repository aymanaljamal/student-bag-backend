package com.studentbag.backend.student.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StudentProfileResponse {
    private Long studentId;
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
    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;
    private Long institutionId;
    private String institutionName;
    private boolean gpaVisibleToParents;
}
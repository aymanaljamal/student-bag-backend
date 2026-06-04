package com.studentbag.backend.instructor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfileResponse {

    private Long id;
    private UUID userId;

    private String externalId;

    private String fullNameArabic;
    private String fullNameEnglish;

    private Long departmentId;
    private String departmentNameArabic;
    private String departmentNameEnglish;

    private Long institutionId;
    private String institutionName;

    private String email;
    private String phone;
    private String avatarUrl;
    private String languageCode;

    private boolean accountConfirmed;
    private boolean active;
    private boolean emailVerified;
    private boolean phoneVerified;
}
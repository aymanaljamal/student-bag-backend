package com.studentbag.backend.instructor.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstructorProfileResponse {

    private Long id;
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
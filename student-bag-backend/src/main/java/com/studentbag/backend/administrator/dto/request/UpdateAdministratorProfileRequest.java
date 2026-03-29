package com.studentbag.backend.administrator.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdministratorProfileRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String languageCode;
    private String adminScope;
    private Long institutionId;
}
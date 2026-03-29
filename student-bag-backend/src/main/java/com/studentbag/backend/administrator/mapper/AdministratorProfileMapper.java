package com.studentbag.backend.administrator.mapper;

import com.studentbag.backend.administrator.dto.response.AdministratorProfileResponse;
import com.studentbag.backend.administrator.entity.Administrator;
import org.springframework.stereotype.Component;

@Component
public class AdministratorProfileMapper {

    public AdministratorProfileResponse toResponse(Administrator administrator) {
        return AdministratorProfileResponse.builder()
                .administratorId(administrator.getId())
                .userId(administrator.getUser().getId())
                .fullName(administrator.getUser().getFullName())
                .email(administrator.getUser().getEmail())
                .phone(administrator.getUser().getPhone())
                .avatarUrl(administrator.getUser().getAvatarUrl())
                .languageCode(administrator.getUser().getLanguageCode())
                .role(administrator.getUser().getRole().name())
                .active(administrator.getUser().isActive())
                .emailVerified(administrator.getUser().isEmailVerified())
                .phoneVerified(administrator.getUser().isPhoneVerified())
                .adminScope(administrator.getAdminScope())
                .institutionId(administrator.getInstitution() != null ? administrator.getInstitution().getId() : null)
                .institutionName(administrator.getInstitution() != null ? administrator.getInstitution().getName() : null)
                .build();
    }
}
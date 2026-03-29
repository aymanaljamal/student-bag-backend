package com.studentbag.backend.administrator.service;

import com.studentbag.backend.administrator.dto.request.UpdateAdministratorProfileRequest;
import com.studentbag.backend.administrator.dto.response.AdministratorProfileResponse;

import java.util.UUID;

public interface AdministratorProfileService {
    AdministratorProfileResponse getMyProfile(UUID userId);
    AdministratorProfileResponse getMyProfileByEmail(String email);
    AdministratorProfileResponse updateMyProfile(UUID userId, UpdateAdministratorProfileRequest request);
    AdministratorProfileResponse updateMyProfileByEmail(String email, UpdateAdministratorProfileRequest request);
}
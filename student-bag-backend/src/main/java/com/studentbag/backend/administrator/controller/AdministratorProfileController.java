package com.studentbag.backend.administrator.controller;

import com.studentbag.backend.administrator.dto.request.UpdateAdministratorProfileRequest;
import com.studentbag.backend.administrator.dto.response.AdministratorProfileResponse;
import com.studentbag.backend.administrator.service.AdministratorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administrator/profile")
@RequiredArgsConstructor
public class AdministratorProfileController {

    private final AdministratorProfileService administratorProfileService;

    @GetMapping("/me")
    public AdministratorProfileResponse getMyProfile(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        return administratorProfileService.getMyProfileByEmail(email);
    }

    @GetMapping("/me/by-email")
    public AdministratorProfileResponse getMyProfileByEmail(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        return administratorProfileService.getMyProfileByEmail(email);
    }

    @PutMapping("/me")
    public AdministratorProfileResponse updateMyProfile(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestBody UpdateAdministratorProfileRequest request
    ) {
        return administratorProfileService.updateMyProfileByEmail(email, request);
    }
}
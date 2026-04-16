package com.studentbag.backend.administrator.service.impl;

import com.studentbag.backend.administrator.dto.request.UpdateAdministratorProfileRequest;
import com.studentbag.backend.administrator.dto.response.AdministratorProfileResponse;
import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.mapper.AdministratorProfileMapper;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.administrator.service.AdministratorProfileService;
import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdministratorProfileServiceImpl implements AdministratorProfileService {

    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final AdministratorProfileMapper administratorProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public AdministratorProfileResponse getMyProfile(UUID userId) {
        Administrator administrator = administratorRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Administrator profile not found for user id: " + userId));

        return administratorProfileMapper.toResponse(administrator);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministratorProfileResponse getMyProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        return getMyProfile(user.getId());
    }

    @Override
    public AdministratorProfileResponse updateMyProfile(UUID userId, UpdateAdministratorProfileRequest request) {
        Administrator administrator = administratorRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Administrator profile not found for user id: " + userId));

        User user = administrator.getUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getLanguageCode() != null) {
            user.setLanguageCode(request.getLanguageCode());
        }

        if (request.getAdminScope() != null) {
            administrator.setAdminScope(request.getAdminScope());
        }

        if (request.getInstitutionId() != null) {
            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Institution not found with id: " + request.getInstitutionId()));
            administrator.setInstitution(institution);
        }

        Administrator saved = administratorRepository.save(administrator);
        return administratorProfileMapper.toResponse(saved);
    }

    @Override
    public AdministratorProfileResponse updateMyProfileByEmail(String email, UpdateAdministratorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        return updateMyProfile(user.getId(), request);
    }
}
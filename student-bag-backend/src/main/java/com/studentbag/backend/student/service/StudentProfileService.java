package com.studentbag.backend.student.service;

import com.studentbag.backend.student.dto.request.UpdateStudentProfileRequest;
import com.studentbag.backend.student.dto.response.StudentProfileResponse;

import java.util.UUID;

public interface StudentProfileService {

    StudentProfileResponse getMyProfile(UUID userId);

    StudentProfileResponse updateMyProfile(UUID userId, UpdateStudentProfileRequest request);

    StudentProfileResponse getMyProfileByEmail(String email);

    StudentProfileResponse updateMyProfileByEmail(String email, UpdateStudentProfileRequest request);
}
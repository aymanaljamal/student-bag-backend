package com.studentbag.backend.instructor.service;

import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;

public interface InstructorService {

    InstructorProfileResponse getPublicProfile(Long instructorId);

    InstructorProfileResponse getMyProfile(String currentUserEmail);
}
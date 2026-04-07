package com.studentbag.backend.instructor.service;

import com.studentbag.backend.institution.entity.Institution;

public interface InstructorAccountIdentityService {

    String generateSystemEmail(String fullNameArabic, Institution institution);
}
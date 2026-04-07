package com.studentbag.backend.instructor.service.impl;

import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.instructor.service.InstructorAccountIdentityService;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class InstructorAccountIdentityServiceImpl implements InstructorAccountIdentityService {

    @Override
    public String generateSystemEmail(String fullNameArabic, Institution institution) {
        String normalized = fullNameArabic == null ? "instructor" : fullNameArabic.trim().replaceAll("\\s+", ".");
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}.]", "");

        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toLowerCase(Locale.ROOT);
        String institutionPart = institution.getId() == null ? "inst" : institution.getId().toString();

        return normalized + "." + uniquePart + "@inst" + institutionPart + ".local";
    }
}

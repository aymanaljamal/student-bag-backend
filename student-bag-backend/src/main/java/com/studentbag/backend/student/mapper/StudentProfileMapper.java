package com.studentbag.backend.student.mapper;

import com.studentbag.backend.student.dto.response.StudentProfileResponse;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class StudentProfileMapper {

    public StudentProfileResponse toResponse(Student student) {
        User user = student.getUser();

        return StudentProfileResponse.builder()
                .studentId(student.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .languageCode(user.getLanguageCode())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .academicLevel(student.getAcademicLevel())
                .schoolGrade(student.getSchoolGrade())
                .universityMajor(student.getUniversityMajor())
                .institutionId(student.getInstitution() != null ? student.getInstitution().getId() : null)
                .institutionName(student.getInstitution() != null ? student.getInstitution().getName() : null)
                .gpaVisibleToParents(student.isGpaVisibleToParents())
                .build();
    }
}
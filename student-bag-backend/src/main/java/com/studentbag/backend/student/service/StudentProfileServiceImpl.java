package com.studentbag.backend.student.service;

import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.student.dto.request.UpdateStudentProfileRequest;
import com.studentbag.backend.student.dto.response.StudentProfileResponse;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.student.service.StudentProfileService;
import com.studentbag.backend.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfile(UUID userId) {
        Student student = studentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        return mapToResponse(student);
    }

    @Override
    public StudentProfileResponse updateMyProfile(UUID userId, UpdateStudentProfileRequest request) {
        Student student = studentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        User user = student.getUser();

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

        if (request.getAcademicLevel() != null) {
            student.setAcademicLevel(request.getAcademicLevel());
        }

        if (request.getSchoolGrade() != null) {
            student.setSchoolGrade(request.getSchoolGrade());
        }

        if (request.getUniversityMajor() != null) {
            student.setUniversityMajor(request.getUniversityMajor());
        }

        if (request.getInstitutionId() != null) {
            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new RuntimeException("Institution not found"));
            student.setInstitution(institution);
        }

        if (request.getGpaVisibleToParents() != null) {
            student.setGpaVisibleToParents(request.getGpaVisibleToParents());
        }

        Student savedStudent = studentRepository.save(student);

        return mapToResponse(savedStudent);
    }

    private StudentProfileResponse mapToResponse(Student student) {
        User user = student.getUser();

        return StudentProfileResponse.builder()
                .studentId(student.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .languageCode(user.getLanguageCode())
                .role(user.getRole().name())
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
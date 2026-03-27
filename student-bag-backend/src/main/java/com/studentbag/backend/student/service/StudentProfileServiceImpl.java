package com.studentbag.backend.student.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.student.dto.request.UpdateStudentProfileRequest;
import com.studentbag.backend.student.dto.response.StudentProfileResponse;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.mapper.StudentProfileMapper;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
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
    private final StudentProfileMapper studentProfileMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfile(UUID userId) {
        Student student = studentRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student profile not found for user id: " + userId));

        return studentProfileMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        return getMyProfile(user.getId());
    }

    @Override
    public StudentProfileResponse updateMyProfile(UUID userId, UpdateStudentProfileRequest request) {
        Student student = studentRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student profile not found for user id: " + userId));

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

        if (request.getUniversityMajor() != null) {
            student.setUniversityMajor(request.getUniversityMajor());
        }

        if (request.getInstitutionId() != null) {
            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Institution not found with id: " + request.getInstitutionId()
                            ));
            student.setInstitution(institution);
        }

        Student savedStudent = studentRepository.save(student);
        return studentProfileMapper.toResponse(savedStudent);
    }

    @Override
    public StudentProfileResponse updateMyProfileByEmail(String email, UpdateStudentProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        return updateMyProfile(user.getId(), request);
    }
}
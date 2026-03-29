package com.studentbag.backend.administrator.service;

import com.studentbag.backend.administrator.dto.request.AdminUpdateManagedUserRequest;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserDetailsResponse;
import com.studentbag.backend.administrator.dto.response.AdminManagedUserSummaryResponse;
import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.auth.service.AuthService;
import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final AdministratorRepository administratorRepository;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminManagedUserSummaryResponse> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(user -> AdminManagedUserSummaryResponse.builder()
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .active(user.isActive())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminManagedUserDetailsResponse getUserDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        return mapToDetails(user);
    }

    @Override
    public AdminManagedUserDetailsResponse updateUserBasicInfo(UUID userId, AdminUpdateManagedUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }

            user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);
        return mapToDetails(savedUser);
    }

    @Override
    public AuthResponse createStudent(StudentRegisterRequest request) {
        return authService.registerStudent(request);
    }

    @Override
    public AuthResponse createInstructor(InstructorRegisterRequest request) {
        return authService.registerInstructor(request);
    }

    private AdminManagedUserDetailsResponse mapToDetails(User user) {
        AdminManagedUserDetailsResponse.AdminManagedUserDetailsResponseBuilder builder =
                AdminManagedUserDetailsResponse.builder()
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .languageCode(user.getLanguageCode())
                        .role(user.getRole().name())
                        .active(user.isActive())
                        .emailVerified(user.isEmailVerified())
                        .phoneVerified(user.isPhoneVerified());

        if (user.getRole() == UserRole.STUDENT) {
            Student student = studentRepository.findByUser_Id(user.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Student profile not found for user id: " + user.getId()));

            builder
                    .domainProfileId(student.getId())
                    .academicLevel(student.getAcademicLevel())
                    .schoolGrade(student.getSchoolGrade())
                    .universityMajor(student.getUniversityMajor())
                    .gpaVisibleToParents(student.isGpaVisibleToParents())
                    .institutionId(student.getInstitution() != null ? student.getInstitution().getId() : null)
                    .institutionName(student.getInstitution() != null ? student.getInstitution().getName() : null);

        } else if (user.getRole() == UserRole.INSTRUCTOR) {
            Instructor instructor = instructorRepository.findByUser_Id(user.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Instructor profile not found for user id: " + user.getId()));

            builder
                    .domainProfileId(instructor.getId())
                    .department(instructor.getDepartment())
                    .institutionId(instructor.getInstitution() != null ? instructor.getInstitution().getId() : null)
                    .institutionName(instructor.getInstitution() != null ? instructor.getInstitution().getName() : null);

        } else if (user.getRole() == UserRole.ADMINISTRATOR) {
            Administrator administrator = administratorRepository.findByUser_Id(user.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Administrator profile not found for user id: " + user.getId()));

            builder
                    .domainProfileId(administrator.getId())
                    .adminScope(administrator.getAdminScope())
                    .institutionId(administrator.getInstitution() != null ? administrator.getInstitution().getId() : null)
                    .institutionName(administrator.getInstitution() != null ? administrator.getInstitution().getName() : null);
        }

        return builder.build();
    }
}
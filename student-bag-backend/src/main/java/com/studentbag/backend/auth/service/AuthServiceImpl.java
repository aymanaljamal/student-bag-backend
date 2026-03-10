package com.studentbag.backend.auth.service;

import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.RegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.parent.entity.Parent;
import com.studentbag.backend.parent.repository.ParentRepository;
import com.studentbag.backend.security.jwt.JwtService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final InstructorRepository instructorRepository;
    private final AdministratorRepository administratorRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build());

        Institution institution = null;
        if (request.getInstitutionId() != null) {
            institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new RuntimeException("Institution not found"));
        }

        if (request.getRole() == UserRole.STUDENT) {
            studentRepository.save(Student.builder()
                    .user(user)
                    .academicLevel(request.getAcademicLevel())
                    .schoolGrade(request.getSchoolGrade())
                    .universityMajor(request.getUniversityMajor())
                    .institution(institution)
                    .gpaVisibleToParents(true)
                    .build());
        } else if (request.getRole() == UserRole.PARENT) {
            parentRepository.save(Parent.builder()
                    .user(user)
                    .defaultRelationshipLabel(request.getDefaultRelationshipLabel() == null ? "Parent" : request.getDefaultRelationshipLabel())
                    .build());
        } else if (request.getRole() == UserRole.INSTRUCTOR) {
            instructorRepository.save(Instructor.builder()
                    .user(user)
                    .department(request.getDepartment())
                    .institution(institution)
                    .build());
        } else if (request.getRole() == UserRole.ADMINISTRATOR) {
            administratorRepository.save(Administrator.builder()
                    .user(user)
                    .adminScope(request.getAdminScope())
                    .institution(institution)
                    .build());
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
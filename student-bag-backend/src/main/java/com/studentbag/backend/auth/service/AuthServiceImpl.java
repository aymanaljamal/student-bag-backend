package com.studentbag.backend.auth.service.impl;

import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.auth.dto.request.AdministratorRegisterRequest;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.ParentRegisterRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.auth.service.AuthService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse registerStudent(StudentRegisterRequest request) {
        validateEmail(request.getEmail());
        User user = userRepository.save(buildBaseUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.STUDENT
        ));

        studentRepository.save(Student.builder()
                .user(user)
                .academicLevel(request.getAcademicLevel())
                .schoolGrade(request.getSchoolGrade())
                .universityMajor(request.getUniversityMajor())
                .gpaVisibleToParents(true)
                .build());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse registerParent(ParentRegisterRequest request) {
        validateEmail(request.getEmail());

        User user = userRepository.save(buildBaseUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.PARENT
        ));

        parentRepository.save(Parent.builder()
                .user(user)
                .defaultRelationshipLabel(
                        request.getDefaultRelationshipLabel() == null || request.getDefaultRelationshipLabel().isBlank()
                                ? "Parent"
                                : request.getDefaultRelationshipLabel()
                )
                .build());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse registerInstructor(InstructorRegisterRequest request) {
        validateEmail(request.getEmail());

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        User user = userRepository.save(buildBaseUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.INSTRUCTOR
        ));

        instructorRepository.save(Instructor.builder()
                .user(user)
                .department(request.getDepartment())
                .institution(institution)
                .build());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse registerAdministrator(AdministratorRegisterRequest request) {
        validateEmail(request.getEmail());

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        User user = userRepository.save(buildBaseUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.ADMINISTRATOR
        ));

        administratorRepository.save(Administrator.builder()
                .user(user)
                .adminScope(request.getAdminScope())
                .institution(institution)
                .build());

        return buildAuthResponse(user);
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

        return buildAuthResponse(user);
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
    }

    private User buildBaseUser(String fullName,
                               String email,
                               String phone,
                               String rawPassword,
                               UserRole role) {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .languageCode("en")
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
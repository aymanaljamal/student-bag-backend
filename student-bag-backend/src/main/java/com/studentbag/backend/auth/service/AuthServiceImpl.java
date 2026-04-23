package com.studentbag.backend.auth.service;

import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.auth.dto.request.AdministratorRegisterRequest;
import com.studentbag.backend.auth.dto.request.InstructorRegisterRequest;
import com.studentbag.backend.auth.dto.request.LoginRequest;
import com.studentbag.backend.auth.dto.request.StudentRegisterRequest;
import com.studentbag.backend.auth.dto.response.AuthResponse;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.security.jwt.JwtService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final AdministratorRepository administratorRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final DepartmentRepository departmentRepository;
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
                .academicLevel(String.valueOf(request.getAcademicLevel()))
                .universityMajor(request.getUniversityMajor())
                .gpaVisibleToParents(true)
                .build());

        return buildAuthResponse(user);
    }
    @Override
    public AuthResponse registerInstructor(InstructorRegisterRequest request) {
        validateEmail(request.getEmail());

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        String fullNameArabic = request.getFullNameArabic().trim();
        String fullNameEnglish = request.getFullNameEnglish() == null
                ? null
                : request.getFullNameEnglish().trim();

        User user = userRepository.save(buildBaseUser(
                fullNameArabic,
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.INSTRUCTOR
        ));

        instructorRepository.save(Instructor.builder()
                .user(user)
                .institution(institution)
                .department(department)
                .fullNameArabic(fullNameArabic)
                .fullNameEnglish(
                        fullNameEnglish == null || fullNameEnglish.isBlank()
                                ? fullNameArabic
                                : fullNameEnglish
                )
                .accountConfirmed(false)
                .build());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse registerAdministrator(AdministratorRegisterRequest request) {
        validateEmail(request.getEmail());

        User user = userRepository.save(buildBaseUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                UserRole.ADMINISTRATOR
        ));

        administratorRepository.save(Administrator.builder()
                .user(user)
                .adminScope(
                        request.getAdminScope() == null || request.getAdminScope().isBlank()
                                ? "FULL"
                                : request.getAdminScope()
                )
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

    @Override
    public void changePasswordByEmail(String email, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (currentPassword.equals(newPassword)) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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

        Long studentId = null;
        Long instructorId = null;
        Long administratorId = null;

        if (user.getRole() == UserRole.STUDENT) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found"));
            studentId = student.getId();
        } else if (user.getRole() == UserRole.INSTRUCTOR) {
            Instructor instructor = instructorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Instructor profile not found"));
            instructorId = instructor.getId();
        } else if (user.getRole() == UserRole.ADMINISTRATOR) {
            Administrator administrator = administratorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Administrator profile not found"));
            administratorId = administrator.getId();
        }

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .studentId(studentId)
                .instructorId(instructorId)
                .administratorId(administratorId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
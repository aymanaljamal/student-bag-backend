package com.studentbag.backend.instructor.service.impl;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.instructor.dto.request.InstructorUpdateProfileRequest;
import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.mapper.InstructorMapper;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.instructor.service.InstructorService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final InstructorMapper instructorMapper;

    @Override
    public InstructorProfileResponse getPublicProfile(Long instructorId) {
        Instructor instructor = instructorRepository.findDetailedById(instructorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Instructor not found with id: " + instructorId));

        return instructorMapper.toProfileResponse(instructor);
    }

    @Override
    public InstructorProfileResponse getMyProfile(String currentUserEmail) {
        Instructor instructor = instructorRepository.findDetailedByUserEmail(currentUserEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Instructor profile not found for email: " + currentUserEmail));

        return instructorMapper.toProfileResponse(instructor);
    }

    @Override
    @Transactional
    public InstructorProfileResponse updateMyProfile(
            String currentUserEmail,
            InstructorUpdateProfileRequest request
    ) {
        Instructor instructor = instructorRepository.findDetailedByUserEmail(currentUserEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Instructor profile not found for email: " + currentUserEmail));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        User user = instructor.getUser();

        String fullNameArabic = request.getFullNameArabic().trim();
        String fullNameEnglish = request.getFullNameEnglish() == null
                ? null
                : request.getFullNameEnglish().trim();

        instructor.setFullNameArabic(fullNameArabic);
        instructor.setFullNameEnglish(
                fullNameEnglish == null || fullNameEnglish.isBlank()
                        ? fullNameArabic
                        : fullNameEnglish
        );
        instructor.setDepartment(department);

        user.setFullName(fullNameArabic);
        user.setPhone(
                request.getPhone() == null || request.getPhone().isBlank()
                        ? null
                        : request.getPhone().trim()
        );
        user.setAvatarUrl(
                request.getAvatarUrl() == null || request.getAvatarUrl().isBlank()
                        ? null
                        : request.getAvatarUrl().trim()
        );
        user.setLanguageCode(
                request.getLanguageCode() == null || request.getLanguageCode().isBlank()
                        ? user.getLanguageCode()
                        : request.getLanguageCode().trim().toLowerCase()
        );

        instructorRepository.save(instructor);
        userRepository.save(user);

        return instructorMapper.toProfileResponse(instructor);
    }
}
package com.studentbag.backend.instructor.service.impl;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.mapper.InstructorMapper;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.instructor.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepository;
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
}
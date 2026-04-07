package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.request.ClassSessionRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.mapper.ClassSessionMapper;
import com.studentbag.backend.courses.repository.ClassSessionRepository;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.courses.service.ClassSessionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of ClassSessionService
 */
@Service
@RequiredArgsConstructor
public class ClassSessionServiceImpl implements ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final ClassSessionMapper classSessionMapper;

    @Override
    public ClassSessionResponseDTO create(ClassSessionRequestDTO request) {

        CourseSection section = courseSectionRepository.findById(request.getCourseSectionId())
                .orElseThrow(() -> new EntityNotFoundException("CourseSection not found"));

        ClassSession session = new ClassSession();
        classSessionMapper.toEntity(request, session, section);

        return classSessionMapper.toResponse(classSessionRepository.save(session));
    }

    @Override
    public ClassSessionResponseDTO update(Long id, ClassSessionRequestDTO request) {

        ClassSession session = classSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClassSession not found"));

        CourseSection section = courseSectionRepository.findById(request.getCourseSectionId())
                .orElseThrow(() -> new EntityNotFoundException("CourseSection not found"));

        classSessionMapper.toEntity(request, session, section);

        return classSessionMapper.toResponse(classSessionRepository.save(session));
    }

    @Override
    public ClassSessionResponseDTO getById(Long id) {
        ClassSession session = classSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClassSession not found"));

        return classSessionMapper.toResponse(session);
    }

    @Override
    public List<ClassSessionResponseDTO> getAll() {
        return classSessionRepository.findAll()
                .stream()
                .map(classSessionMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!classSessionRepository.existsById(id)) {
            throw new EntityNotFoundException("ClassSession not found");
        }
        classSessionRepository.deleteById(id);
    }
}
package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.request.CourseSectionRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseSectionResponseDTO;
import com.studentbag.backend.courses.entity.*;
import com.studentbag.backend.courses.mapper.CourseSectionMapper;
import com.studentbag.backend.courses.repository.*;
import com.studentbag.backend.courses.service.CourseSectionService;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSectionServiceImpl implements CourseSectionService {

    private final CourseSectionRepository repository;
    private final CourseRepository courseRepository;
    private final TermRepository termRepository;
    private final InstructorRepository instructorRepository;
    private final CourseSectionMapper mapper;

    @Override
    public CourseSectionResponseDTO create(CourseSectionRequestDTO request) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));

        Instructor instructor = request.getInstructorId() != null
                ? instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found"))
                : null;

        CourseSection parent = request.getParentLectureSectionId() != null
                ? repository.findById(request.getParentLectureSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Parent section not found"))
                : null;

        CourseSection section = new CourseSection();
        mapper.toEntity(request, section, course, term, instructor, parent);

        return mapper.toResponse(repository.save(section));
    }

    @Override
    public CourseSectionResponseDTO update(Long id, CourseSectionRequestDTO request) {

        CourseSection section = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));

        Instructor instructor = request.getInstructorId() != null
                ? instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found"))
                : null;

        CourseSection parent = request.getParentLectureSectionId() != null
                ? repository.findById(request.getParentLectureSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Parent section not found"))
                : null;

        mapper.toEntity(request, section, course, term, instructor, parent);

        return mapper.toResponse(repository.save(section));
    }

    @Override
    public CourseSectionResponseDTO getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
    }

    @Override
    public List<CourseSectionResponseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Section not found");
        }
        repository.deleteById(id);
    }
}
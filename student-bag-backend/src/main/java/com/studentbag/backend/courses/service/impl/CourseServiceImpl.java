package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.request.CourseRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseResponseDTO;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.mapper.CourseMapper;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.specification.CourseSpecification;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of CourseService with advanced search and optional sections
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponseDTO create(CourseRequestDTO request) {

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        Course course = new Course();
        courseMapper.toEntity(request, course, institution, department);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO request) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        courseMapper.toEntity(request, course, institution, department);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO getById(Long id, boolean includeSections) {
        return courseRepository.findById(id)
                .map(course -> courseMapper.toResponse(course, includeSections))
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    @Override
    public List<CourseResponseDTO> getAll(boolean includeSections) {
        return courseRepository.findAll()
                .stream()
                .map(course -> courseMapper.toResponse(course, includeSections))
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    @Override
    public Page<CourseResponseDTO> search(
            String keyword,
            Long institutionId,
            String level,
            Boolean isActive,
            boolean includeSections,
            Pageable pageable
    ) {
        return courseRepository.findAll(
                        CourseSpecification.search(keyword, institutionId, level, isActive),
                        pageable
                )
                .map(course -> courseMapper.toResponse(course, includeSections));
    }
}
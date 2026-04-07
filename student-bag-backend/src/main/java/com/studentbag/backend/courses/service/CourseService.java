package com.studentbag.backend.courses.service;

import com.studentbag.backend.courses.dto.request.CourseRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing Courses (advanced)
 */
public interface CourseService {

    /**
     * Create new course
     */
    CourseResponseDTO create(CourseRequestDTO request);

    /**
     * Update existing course
     */
    CourseResponseDTO update(Long id, CourseRequestDTO request);

    /**
     * Get course by ID
     * @param includeSections whether to include course sections in response
     */
    CourseResponseDTO getById(Long id, boolean includeSections);

    /**
     * Get all courses
     * @param includeSections whether to include course sections in response
     */
    List<CourseResponseDTO> getAll(boolean includeSections);

    /**
     * Delete course by ID
     */
    void delete(Long id);

    /**
     * Advanced search with pagination and optional sections
     * @param keyword search by name/code
     * @param institutionId filter by institution
     * @param level filter by academic level
     * @param isActive filter active/inactive courses
     * @param includeSections include course sections in response
     * @param pageable pagination info
     */
    Page<CourseResponseDTO> search(
            String keyword,
            Long institutionId,
            String level,
            Boolean isActive,
            boolean includeSections,
            Pageable pageable
    );
}
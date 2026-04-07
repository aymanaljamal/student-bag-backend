package com.studentbag.backend.courses.service;

import com.studentbag.backend.courses.dto.request.CourseSectionRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseSectionResponseDTO;

import java.util.List;

public interface CourseSectionService {

    CourseSectionResponseDTO create(CourseSectionRequestDTO request);

    CourseSectionResponseDTO update(Long id, CourseSectionRequestDTO request);

    CourseSectionResponseDTO getById(Long id);

    List<CourseSectionResponseDTO> getAll();

    void delete(Long id);
}
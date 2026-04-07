package com.studentbag.backend.courses.service;

import com.studentbag.backend.courses.dto.request.FacultyRequestDTO;
import com.studentbag.backend.courses.dto.response.FacultyResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FacultyService {

    FacultyResponseDTO create(FacultyRequestDTO request);

    FacultyResponseDTO update(Long id, FacultyRequestDTO request);

    FacultyResponseDTO getById(Long id);

    List<FacultyResponseDTO> getAll();

    void delete(Long id);

    Page<FacultyResponseDTO> search(String keyword, Long institutionId, Boolean isActive, Pageable pageable);
}
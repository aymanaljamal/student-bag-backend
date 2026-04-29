package com.studentbag.backend.courses.service;

import com.studentbag.backend.courses.dto.request.DepartmentRequestDTO;
import com.studentbag.backend.courses.dto.response.DepartmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDTO create(DepartmentRequestDTO request);

    DepartmentResponseDTO update(Long id, DepartmentRequestDTO request);

    DepartmentResponseDTO getById(Long id);

    List<DepartmentResponseDTO> getAll();

    List<DepartmentResponseDTO> getAllByInstitution(Long institutionId);

    void delete(Long id);

    Page<DepartmentResponseDTO> search(
            String keyword,
            Long institutionId,
            Long facultyId,
            Boolean isActive,
            Pageable pageable
    );
}
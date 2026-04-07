package com.studentbag.backend.courses.service;
import com.studentbag.backend.courses.dto.request.ClassSessionRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;

import java.util.List;

public interface ClassSessionService {

    ClassSessionResponseDTO create(ClassSessionRequestDTO request);

    ClassSessionResponseDTO update(Long id, ClassSessionRequestDTO request);

    ClassSessionResponseDTO getById(Long id);

    List<ClassSessionResponseDTO> getAll();

    void delete(Long id);
}